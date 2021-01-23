package com.personal.project.explora.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "episodes_table")
public class Episode {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int year;
    private String title;
    private String description;
    private String length;
    private String link;
    private String image;
    private int downloadId;

    public Episode(int year, String title, String description, String length, String link, String image) {
        this.year = year;
        this.title = title;
        this.description = description;
        this.length = length;
        this.link = link;
        this.image = image;
        this.downloadId = -1;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDownloadId(int download_id) {
        this.downloadId = download_id;
    }

    public int getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLength() {
        return length;
    }

    public String getLink() {
        return link;
    }

    public String getImage() {
        return image;
    }

    public int getDownloadId() {
        return downloadId;
    }

    @Ignore
    @Override
    public String toString() {
        return "[ ID: " + id + "; " +
                "Year: " + year + "; " +
                "Title: " + title + "; " +
                "Description: " + description + "; " +
                "Length: " + length + "; " +
                "Link: " + link + "; " +
                "Image: " + image + "; " +
                "DownloadID: " + downloadId + " ]";
    }
}
