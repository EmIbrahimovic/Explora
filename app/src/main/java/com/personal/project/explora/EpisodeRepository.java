package com.personal.project.explora;

import android.app.Application;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.db.EpisodeDao;
import com.personal.project.explora.db.EpisodeDatabase;
import com.personal.project.explora.feed.Channel;
import com.personal.project.explora.feed.FeedAPI;
import com.personal.project.explora.feed.Item;
import com.personal.project.explora.feed.Rss;
import com.personal.project.explora.service.download.DownloadUtil;
import com.personal.project.explora.utils.DateUtil;
import com.personal.project.explora.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class EpisodeRepository {

    private static final String TAG = "EpisodeRepository";

    private final AppExecutors mExecutors;

    private static EpisodeRepository instance;

    private final EpisodeDao episodeDao;
    private final FeedAPI feedAPI;

    private static final String BASE_URL = "https://feed.hrt.hr/";
    private static final String FEED_URL = "https://feed.hrt.hr/podcast/explora.xml";

    private final LiveData<List<Integer>> years;

    private final MutableLiveData<Integer> networkOperationSucceeded;
    public static final int SUCCESS = 1;
    public static final int FAILURE = -1;
    public static final int LOADING = 0;

    /*
        CONSTRUCTOR AND RELATED METHODS
     */

    private EpisodeRepository(Application application, AppExecutors executors) {

        mExecutors = executors;

        EpisodeDatabase database = ((BasicApp)application).getDatabase();
        episodeDao = database.episodeDao();

        DownloadUtil.setRealDownloadsState(application);

        feedAPI = buildFeedAPI();
        networkOperationSucceeded = new MutableLiveData<>();
        networkOperationSucceeded.postValue(null);

        years = episodeDao.getYears();
    }

    public static EpisodeRepository getInstance(Application application, AppExecutors executors) {

        if (instance == null) {
            synchronized (EpisodeRepository.class) {
                if (instance == null) {
                    instance = new EpisodeRepository(application, executors);
                }
            }
        }
        return instance;
    }

    private FeedAPI buildFeedAPI() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .callbackExecutor(mExecutors.networkIO())
                .build();

        return retrofit.create(FeedAPI.class);
    }

    /*
        GETTERS
     */

    public LiveData<List<Integer>> getYears() {
        return years;
    }

    public LiveData<List<Episode>> getEpisodesFromYear(int year) {
        return episodeDao.getEpisodesFromYear(year);
    }

    public LiveData<List<Episode>> getRecentEpisodes() {
        return episodeDao.getRecentEpisodes();
    }

    public LiveData<List<Episode>> getDownloadedEpisodes() {
        return episodeDao.getDownloadedEpisodes();
    }

    public LiveData<Integer> getNetworkOperationStatus() {
        return networkOperationSucceeded;
    }

    /*
        DATABASE OPERATIONS
     */

    public void insert(final Episode episode) {
        mExecutors.diskIO().execute(() -> episodeDao.insert(episode));
    }

    public void update(final Episode episode) {
        mExecutors.diskIO().execute(() -> episodeDao.update(episode));
    }

    public void delete(final Episode episode) {
        mExecutors.diskIO().execute(() -> episodeDao.delete(episode));
    }

    /** very special use case with a probably better solution but hey */
    public void getEpisodeFromIdForPrepare(int id, EpisodeRetrievedListenerForPrepare listener,
                                           boolean playWhenReady, Bundle extras) {
        mExecutors.diskIO().execute(() -> {
            Episode episode = episodeDao.getEpisodeSync(id);
            listener.getHandler().post(
                    () -> listener.onEpisodeRetrieved(episode, playWhenReady, extras));
        });
    }


    public void getFromIdAndUpdateDownloadId(int id, int downloadId) {
        if (id < 0 || !Episode.isValidDownloadId(downloadId))
            return;

        mExecutors.diskIO().execute(() -> {
            Episode episode = episodeDao.getEpisodeSync(id);
            if (episode != null) {
                episode.setDownloadState(downloadId);
                update(episode);
            }
        });
    }


    public void updateDownloads(List<Integer> downloadedIds) {
        mExecutors.diskIO().execute(() -> {

            Log.d(TAG, "updateDownloads: updating the initial download state");
            List<Integer> currentlyDownloaded = episodeDao.getDownloadedEpisodesIdSync();
            for (Integer id : currentlyDownloaded) {
                if (!downloadedIds.contains(id))
                    getFromIdAndUpdateDownloadId(id, Episode.NOT_DOWNLOADED);
            }

            for (Integer id : downloadedIds) {
                if (!currentlyDownloaded.contains(id))
                    getFromIdAndUpdateDownloadId(id, Episode.DOWNLOADED);
            }

        });
    }

    /*
        NETWORK OPERATIONS
     */

    public void refreshNewEpisodes() {

        networkOperationSucceeded.postValue(LOADING);

        Call<Rss> rssCall = feedAPI.getRss(FEED_URL);
        rssCall.enqueue(new Callback<Rss>() {
            @Override
            public void onResponse(@NotNull Call<Rss> call, @NotNull Response<Rss> response) {
                Log.d(TAG, "onResponse: " + response.code());
                if (!response.isSuccessful() || response.body() == null) {
                    networkOperationSucceeded.postValue(FAILURE);
                    return;
                }

                Channel channel = response.body().getChannel();

                updateDBWithItems(channel.getItems());
            }

            @Override
            public void onFailure(@NotNull Call<Rss> call, @NotNull Throwable t) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS " + t.getMessage());
                networkOperationSucceeded.postValue(FAILURE);
            }
        });
    }

    private void updateDBWithItems(List<Item> items) {

        mExecutors.networkIO().execute(() -> {

            // STEP 1: Check if all episodes currently in my database still exist in the RSS feed
            List<Episode> currentEpisodeList = episodeDao.getEpisodesSync();
            if (currentEpisodeList != null) {
                for (Episode episode : currentEpisodeList) {
                    Log.d(TAG, "updateDBWithItems: STEP 1 processing " + episode);
                    boolean found = false;
                    for (Item item : items) {
                        if (DateUtil.formatMyDate(DateUtil.parse(item.getDate())).equals(
                                episode.getDatePublished())) {
                            found = true;
                            break;
                        }
                    }

                    if (!found)
                        delete(episode);
                    else
                        Log.d(TAG, "updateDBWithItems: STEP 1 episode found");
                }
            }

            // STEP 2: Add episodes from the RSS feed to my database if needed
            //         Mark episodes as unplayable if necessary
            for (Item item : items) {
                Log.d(TAG, "updateDBWithItems: processing item " + item);

                long duration = item.getDuration() * 1000 - 4000;
                try {
                    duration = getDuration(item.getLink());
                } catch (Exception e) {
                    Log.e(TAG, "updateDBWithItems: getDuration throws error " + e.getMessage());
                }

                Episode newEpisode = new Episode(
                        item.getYear(),
                        item.getDescription(),
                        item.getLink(),
                        item.getShareLink(),
                        DateUtil.formatMyDate(DateUtil.parse(item.getDate())),
                        duration
                );

                Episode lookup = episodeDao.getEpisodeByDatePublished(newEpisode.getDatePublished());

                if (lookup == null) {
                    if (!StringUtils.isEmpty(newEpisode.getLink())) {
                        insert(newEpisode);
                    }
                }
                else if (newEpisode.completes(lookup)) {
                    lookup.completeContentWith(newEpisode);
                    update(lookup);
                }
            }

            mExecutors.diskIO().execute(() -> networkOperationSucceeded.postValue(SUCCESS));
        });
    }

    private long getDuration(String episodeLink) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(episodeLink, new HashMap<String, String>());

        return Long.parseLong(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    public interface EpisodeRetrievedListenerForPrepare {

        Handler getHandler();

        void onEpisodeRetrieved(Episode episode, boolean playWhenReady, Bundle extras);
    }

    /*
    DOWNLOAD OPERATIONS
     */

    public void download(Episode episode, Context applicationContext) {
        DownloadUtil.addDownload(episode, applicationContext);
    }

    public void removeDownload(Episode episode, Context applicationContext) {
        DownloadUtil.removeDownload(episode, applicationContext);
    }

    public void stopDownload(Episode episode, Context applicationContext) {
        DownloadUtil.stopDownload(episode, applicationContext);
    }
}
