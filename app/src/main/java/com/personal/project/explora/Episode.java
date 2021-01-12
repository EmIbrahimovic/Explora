package com.personal.project.explora;

import android.net.Uri;

public class Episode {

    private int id;

    private String title;
    private String description;
    private int lastTime;

    //public Episode(String title, String description, Uri imageUri
    // TODO fix n figure out Episode.java: time, image, others(?)

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getLastTime() {
        return lastTime;
    }
}
