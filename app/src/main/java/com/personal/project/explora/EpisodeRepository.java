package com.personal.project.explora;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.db.EpisodeDao;
import com.personal.project.explora.db.EpisodeDatabase;
import com.personal.project.explora.feed.Channel;
import com.personal.project.explora.feed.FeedAPI;
import com.personal.project.explora.feed.Rss;
import com.personal.project.explora.utils.ObjectUtil;

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
    private String feedLastUpdate;

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

    /*public void delete(final Episode episode) {
        mExecutors.diskIO().execute(() -> episodeDao.delete(episode));
    }*/

    /*
        NETWORK OPERATIONS
     */

    private void refreshRecents() {

        Call<Rss> rssCall = feedAPI.getRss(FEED_URL);
        rssCall.enqueue(new Callback<Rss>() {
            @Override
            public void onResponse(Call<Rss> call, Response<Rss> response) {
                Log.d(TAG, "onResponse: " + response.code());
                if (!response.isSuccessful() || response.body() == null) {
                    networkOperationSucceeded.postValue(FAILURE);
                    return;
                }

                Channel channel = response.body().getChannel();
                recents = channel.getEpisodes();
                feedLastUpdate = channel.getLastBuildDate();
                updateDB(recents);
                networkOperationSucceeded.postValue(SUCCESS);
            }

            @Override
            public void onFailure(Call<Rss> call, Throwable t) {
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

                if (lookup == null) {
                    if (!ObjectUtil.isEmpty(recentEpisode.getLink())) {
                        toInsert.add(recentEpisode);
                        //insert(recentEpisode);
                    }
                }
                else if (lookup.getLastUpdated().equals(recentEpisode.getLastUpdated())) {
                    break;
                }
                else if (recentEpisode.completes(lookup)) {

                    lookup.completeWith(recentEpisode);

                    if (lookup.getDownloadId() != Episode.NOT_DOWNLOADED) {
                        // TODO choose behaviour for when an updated episode was already downloaded
                    }

                    episodeDao.update(lookup);
                }
            }

            for (int i = toInsert.size() - 1; i >= 0; i--) {
                insert(toInsert.get(i));
            }
        });
    }

}
