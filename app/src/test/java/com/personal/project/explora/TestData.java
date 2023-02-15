package com.personal.project.explora;

import com.personal.project.explora.db.Episode;

import java.util.Arrays;
import java.util.List;

public class TestData {

    static final Episode EMPTY_EPISODE = new Episode(0, null,
            null, null,  null, null, null);

    static final Episode EPISODE = new Episode(3000, "title1",
            "desc1", "1h",  "link1.com", "image1.jpg", "01.01.2049.");
    static final Episode EPISODE2 = new Episode(3000, "title2",
            "desc2", "2h",  "link2.com", "image2.jpg", "01.01.2049.");

    static final Episode INEPISODE = new Episode(3000, "title1",
            "desc1", null,  "link1.com", "image1.jpg", "01.01.2048.");
    static final Episode INEPISODE2 = new Episode(3000, "title2",
            null, null,  "link2.com", null, "01.01.2048.");
    static final Episode INEPISODE3 = new Episode(3000, "title3",
            null, null,  null, null, null);

    static final Episode ENEPISODE = new Episode(3000, "title1",
            null, "1h",  null, null, null);
    static final Episode ENEPISODE2 = new Episode(3000, "title2",
            "desc2", null,  "link2.com", null, "01.01.2049.");
    static final Episode ENEPISODE3 = new Episode(3000, "title3",
            null, null,  null, null, null);
}
