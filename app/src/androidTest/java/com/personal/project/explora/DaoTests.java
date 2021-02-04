package com.personal.project.explora;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.LongDef;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.db.EpisodeDao;
import com.personal.project.explora.db.EpisodeDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.personal.project.explora.TestData.*;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DaoTests {

    private static final String TAG = "DatabaseTests";

    AppExecutors ex;
    EpisodeDatabase db;
    EpisodeDao dao;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        ex = new AppExecutors();
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                EpisodeDatabase.class)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build();

        dao = db.episodeDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void getEpisodeByYear() throws InterruptedException {

        dao.insert(EPISODE);
        List<Episode> episodeList = 
                LiveDataTestUtil.getValue(dao.getEpisodesFromYear(EPISODE.getYear()));

        for (Episode ep : episodeList) {
            System.out.println(ep);
            Log.d(TAG, "getEpisodeByYear: " + ep);
        }

        assertNotNull(episodeList);
        assertFalse(episodeList.isEmpty());
        assertEquals(1, episodeList.size());
        assertEquals(EPISODE.getYear(), episodeList.get(0).getYear());

        dao.delete(episodeList.get(0));

        episodeList =
                LiveDataTestUtil.getValue(dao.getEpisodesFromYear(EPISODE.getYear()));
        assertEquals(0, episodeList.size());
        assertTrue(episodeList.isEmpty());
    }

    @Test
    public void getEpisodesByYear() throws InterruptedException {

        dao.insert(EPISODE);
        dao.insert(EPISODE2);
        List<Episode> episodeList =
                LiveDataTestUtil.getValue(dao.getEpisodesFromYear(EPISODE.getYear()));

        assertNotNull(episodeList);
        assertFalse(episodeList.isEmpty());
        assertEquals(2, episodeList.size());
        assertEquals(EPISODE.getYear(), episodeList.get(0).getYear());

        //dao.delete(EPISODE);
        //dao.delete(EPISODE2);
    }

    @Test
    public void getSingleEpisodeById() {

        dao.insert(EPISODE);
        Episode ep = dao.getEpisode(1);

        assertNotNull(ep);
        assertEquals(EPISODE.getYear(), ep.getYear());
        assertEquals(EPISODE.getTitle(), ep.getTitle());
        assertEquals(EPISODE.getDescription(), ep.getDescription());
        assertEquals(EPISODE.getLength(), ep.getLength());
        assertEquals(EPISODE.getLink(), ep.getLink());
        assertEquals(EPISODE.getImage(), ep.getImage());
        assertEquals(EPISODE.getDownloadId(), ep.getDownloadId());

        //dao.delete(EPISODE);
    }

    @Test
    public void seeWhatHappensWhenDataIsntThere() {
        Episode episode = dao.getEpisode(3000);
        Log.d(TAG, "seeWhatHappensWhenDataIsntThere: " + episode);
    }

    @Test
    public void seeWhatHappensWhenDataIsntThere2() {
        Episode episode = dao.getEpisodeByTitle("nonexistent title");
        Log.d(TAG, "seeWhatHappensWhenDataIsntThere2: " + episode);
    }

}