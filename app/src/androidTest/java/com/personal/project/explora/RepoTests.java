package com.personal.project.explora;

import android.content.Context;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.personal.project.explora.db.Episode;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RepoTests {

    private static final String TAG = "DatabaseTests";

    AppExecutors ex;
    EpisodeRepository repo;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        ex = new AppExecutors();
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        repo = EpisodeRepository.getInstance(appContext, ex);
    }

    /*@Test
    public void getExistingYears() throws InterruptedException {

        LiveData<List<Integer>> years = repo.getYears();

        Log.d(TAG, "getExistingYears: " + LiveDataTestUtil.getValue(years));
        // getExistingEpisodes: [2021, 2020, 2019, 2018, 2017]
    }*/

    @Test
    public void getEpisodeByYear() throws InterruptedException {

        assertNotNull(repo.getEpisodesFromYear(2017));
        List<Episode> episodeList =
                LiveDataTestUtil.getValue(repo.getEpisodesFromYear(2017));

        assertNotNull(episodeList);
        assertFalse(episodeList.isEmpty());
        for (Episode ep : episodeList) {
            Log.d(TAG, "getEpisodeByYear 2017: " + ep);
        }
    }

//    @Test
//    public void

}