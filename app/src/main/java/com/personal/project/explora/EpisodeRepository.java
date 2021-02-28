package com.personal.project.explora;

import android.app.Application;
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
import com.personal.project.explora.feed.Rss;
import com.personal.project.explora.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class EpisodeRepository {

    private static final String TAG = "EpisodeRepository";

    private AppExecutors mExecutors;

    private static EpisodeRepository instance;

    private EpisodeDao episodeDao;
    private FeedAPI feedAPI;

    private static final String BASE_URL = "https://radio.hrt.hr/";
    private static final String FEED_URL = "https://radio.hrt.hr/podcast/rss/radio-pula/1277/explora.xml";

    private Map<Integer, LiveData<List<Episode>>> allEpisodes;
    private List<Episode> recents;

    private MutableLiveData<Integer> networkOperationSucceeded;
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

        feedAPI = buildFeedAPI();
        networkOperationSucceeded = new MutableLiveData<>();
        networkOperationSucceeded.setValue(LOADING);
        refreshRecents();

        allEpisodes = populateEpisodeMap();
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

    private Map<Integer, LiveData<List<Episode>>> populateEpisodeMap() {

        Map<Integer, LiveData<List<Episode>>> episodeMap = new HashMap<>();
        for (int year = 2017; year < 2022; year++) {
            episodeMap.put(year, episodeDao.getEpisodesFromYear(year));
        }
        return episodeMap;
    }

    /*
        GETTERS
     */

    public LiveData<List<Episode>> getEpisodesFromYear(int year) {
        return allEpisodes.get(year);
    }

    public Map<Integer, LiveData<List<Episode>>> getAllEpisodes() {
        return allEpisodes;
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
        mExecutors.diskIO().execute(() -> {
            episodeDao.update(episode);
            Log.d(TAG, "update: updated this guy " + episode);
        });
    }

    /*public void delete(final Episode episode) {
        mExecutors.diskIO().execute(() -> episodeDao.delete(episode));
    }*/

//    public void getEpisodeFromId(int id, EpisodeRetrievedListener listener) {
//        mExecutors.diskIO().execute(() -> {
//            Episode episode = episodeDao.getEpisodeSync(id);
//            listener.onEpisodeRetrieved(episode);
//        });
//    }

    /** very special use case with a probably better solution but hey */
    public void getEpisodeFromIdForPrepare(int id, EpisodeRetrievedListenerForPrepare listener,
                                           boolean playWhenReady, Bundle extras) {
        mExecutors.diskIO().execute(() -> {
            Episode episode = episodeDao.getEpisodeSync(id);
            Log.d(TAG, "getEpisodeFromIdForPrepare: needed episode with id=" + id + "; gotten episode=" + episode);
            listener.getHandler().post(
                    () -> listener.onEpisodeRetrieved(episode, playWhenReady, extras));
        });
    }


    public void getFromIdAndUpdateDownloadId(int id, int downloadId) {
        if (id < 0 || !Episode.isValidDownloadId(downloadId))
            return;

        mExecutors.diskIO().execute(() -> {
            Episode episode = episodeDao.getEpisodeSync(id);
            episode.setDownloadState(downloadId);
            update(episode);
        });
    }

    /*
        NETWORK OPERATIONS
     */

    public void refreshRecents() {

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
                recents = channel.getEpisodes();

                // updateDB posts success
                updateDB(recents);
            }

            @Override
            public void onFailure(@NotNull Call<Rss> call, @NotNull Throwable t) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS " + t.getMessage());
                networkOperationSucceeded.postValue(FAILURE);
            }
        });
    }

    private void updateDB(List<Episode> recentEpisodes) {

        mExecutors.diskIO().execute(() -> {
            List<Episode> toInsert = new ArrayList<>();
            for (Episode recentEpisode : recentEpisodes) {
                Episode lookup = episodeDao.getEpisodeByTitle(recentEpisode.getTitle());
                recentEpisode.setDuration(getDuration(recentEpisode));

                if (lookup == null) {
                    if (!StringUtils.isEmpty(recentEpisode.getLink())) {
                        toInsert.add(recentEpisode);
                    }
                }
                else if (lookup.getLastUpdated().equals(recentEpisode.getLastUpdated())) {
                    break;
                }
                else if (recentEpisode.completes(lookup)) {

                    lookup.completeContentWith(recentEpisode);

                    update(lookup);
                }
            }

            for (int i = toInsert.size() - 1; i >= 0; i--) {
                insert(toInsert.get(i));
            }

            mExecutors.diskIO().execute(() -> networkOperationSucceeded.postValue(SUCCESS));
        });
    }

    private long getDuration(Episode episode) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(episode.getLink(), new HashMap<>());

        return Long.parseLong(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    public interface EpisodeRetrievedListenerForPrepare {
        Handler getHandler();

        void onEpisodeRetrieved(Episode episode, boolean playWhenReady, Bundle extras);
    }

    public interface EpisodeRetrievedListener {
        void onEpisodeRetrieved(Episode episode);
    }

}
