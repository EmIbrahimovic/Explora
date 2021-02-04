package com.personal.project.explora;

import com.personal.project.explora.db.Episode;

import org.junit.Before;
import org.junit.Test;

import static com.personal.project.explora.TestData.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EpisodeTests {
    
    Episode episode;
    Episode episode2;
    
    Episode inepisode;
    Episode inepisode2;
    Episode inepisode3;
    
    Episode enepisode;
    Episode enepisode2;
    Episode enepisode3;
    
    @Before
    public void setup() {
        episode = new Episode(EPISODE);
        episode2 = new Episode(EPISODE2);
        
        inepisode = new Episode(INEPISODE);
        inepisode2 = new Episode(INEPISODE2);
        inepisode3 = new Episode(INEPISODE3);
        
        enepisode = new Episode(ENEPISODE);
        enepisode2 = new Episode(ENEPISODE2);
        enepisode3 = new Episode(ENEPISODE3);
    }
    
    @Test
    public void completeEpisodeIsComplete() {
        episode.setId(1);
        episode2.setId(2);
        assertTrue(episode.isComplete());
        assertTrue(episode2.isComplete());
    }

    @Test
    public void incompleteEpisodeIsNotComplete() {
        inepisode.setId(1);
        inepisode2.setId(2);
        assertFalse(inepisode.isComplete());
        assertFalse(inepisode2.isComplete());
        assertFalse(inepisode3.isComplete());
    }

    @Test
    public void completeEpisodeCompletesIncomplete() {
        inepisode.setId(1);
        inepisode2.setId(2);
        assertTrue(episode.completes(inepisode));
        assertTrue(episode2.completes(inepisode2));
    }

    @Test
    public void completeEpisodeDoesNotCompleteIncomplete() {
        inepisode2.setId(2);
        assertFalse(episode.completes(inepisode2));
    }

    @Test
    public void incompleteEpisodeCompletesIncomplete() {
        inepisode.setId(1);
        inepisode2.setId(2);
        inepisode3.setId(3);
        assertTrue(enepisode.completes(inepisode));
        assertTrue(enepisode2.completes(inepisode2));
    }

    @Test
    public void completeCompleteWithIncomplete() {
        inepisode.setId(1);
        inepisode2.setId(2);
        episode.setId(1);
        episode2.setId(2);

        inepisode.completeWith(episode);
        inepisode2.completeWith(episode2);
        assertEquals(inepisode, episode);
        assertEquals(inepisode2, episode2);
    }

    @Test
    public void completeIncompleteWithIncomplete() {
        inepisode.setId(1);
        inepisode2.setId(2);

        inepisode.completeWith(enepisode);
        inepisode2.completeWith(enepisode2);

        Episode result1 = new Episode(3000, "title1",
                "desc1", "1h",  "link1.com", "image1.jpg", "01.01.2048.");
        result1.setId(1);
        Episode result2 = new Episode(3000, "title2",
                "desc2", null,  "link2.com", null, "01.01.2049.");
        result2.setId(2);

        assertEquals(inepisode, result1);
        assertEquals(inepisode2, result2);
    }
}
