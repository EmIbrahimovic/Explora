package com.personal.project.explora;

import com.personal.project.explora.db.Episode;

import java.util.Arrays;
import java.util.List;

public class TestData {

    static final Episode EPISODE = new Episode(3000, "title1",
            "desc1", "1h",  "link1.com", "image1.jpg");
    static final Episode EPISODE2 = new Episode(3000, "title2",
            "desc2", "2h",  "link2.com", "image2.jpg");

    static final List<Episode> EPISODES = Arrays.asList(EPISODE, EPISODE2);
}
