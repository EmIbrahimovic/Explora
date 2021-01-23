package com.personal.project.explora;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.db.EpisodeDao;
import com.personal.project.explora.db.EpisodeDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EpisodeRepository {

    private AppExecutors mExecutors;

    private static EpisodeRepository instance;

    private EpisodeDao episodeDao;
    private Map<Integer, LiveData<List<Episode>>> allEpisodes;

    private EpisodeRepository(Context appContext, AppExecutors executors) {

        mExecutors = executors;

        EpisodeDatabase database = EpisodeDatabase.getInstance(appContext);
        episodeDao = database.episodeDao();

        allEpisodes = new HashMap<>();
        populateEpisodeMap();
    }

    public static EpisodeRepository getInstance(Context appContext, AppExecutors executors) {

        if (instance == null) {
            synchronized (EpisodeRepository.class) {
                if (instance == null) {
                    instance = new EpisodeRepository(appContext, executors);
                }
            }
        }
        return instance;
    }

    private void populateEpisodeMap() {
        for (int year = 2017; year <= 2021; year++) {
            allEpisodes.put(year, episodeDao.getEpisodesFromYear(year));
        }
    }

    public void insert(final Episode episode) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                episodeDao.insert(episode);
            }
        });
    }

    public void update(final Episode episode) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                episodeDao.update(episode);
            }
        });
    }

    public LiveData<List<Episode>> getEpisodesFromYear(int year) {
        if (year == 2021)
            refreshRecents();

        return allEpisodes.get(year);
    }

    private void refreshRecents() {

    }

}
