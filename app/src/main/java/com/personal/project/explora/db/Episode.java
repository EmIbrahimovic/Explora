package com.personal.project.explora.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.personal.project.explora.utils.DateUtil;
import com.personal.project.explora.utils.ObjectUtil;

import java.time.LocalDate;
import java.util.Objects;

@Entity(tableName = "episodes_table")
public class Episode {

    private static final String CREATION_DATE = "12.01.2021.";
    public static final int NOT_DOWNLOADED = -1;

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int year;
    private String title;
    private String description;
    private String length;
    private String link;
    private String image;
    private String lastUpdated;
    private int downloadId;

    public Episode(int year, String title, String description, String length, String link, String image, String lastUpdated) {
        this.year = year;
        this.title = title;
        this.description = description;
        this.length = length;
        this.link = link;
        this.image = image;
        this.lastUpdated = lastUpdated;
        this.downloadId = NOT_DOWNLOADED;
    }

    public Episode(Episode other) {
        this.id = other.id;
        this.year = other.year;
        this.title = other.title;
        this.description = other.description;
        this.length = other.length;
        this.link = other.link;
        this.image = other.image;
        this.lastUpdated = other.lastUpdated;
        this.downloadId = other.downloadId;
    }

    public Episode(String link, String description, LocalDate date, LocalDate lastUpdated) {
        this(date.getYear(),
                "Emisija " + DateUtil.formatMyDate(date),
                description,
                null,
                link,
                null,
                DateUtil.formatMyDate(lastUpdated));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getLastUpdated() {
        return lastUpdated;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int download_id) {
        this.downloadId = download_id;
    }

    /**
     Checks if other episode has some non-null or non-empty attributes which this episode has.
     Only performs check if episode titles match.
     Episode which is being completed must have id and title (by extension also year).
     */
    public boolean completes(Episode other) {
        if (other.isComplete())
            return false;
        
        if (!this.title.equals(other.title))
            return false;
        
        boolean ret = false;
        if (ObjectUtil.isEmpty(other.description) &&
                !ObjectUtil.isEmpty(this.description))
            ret = true;

        if (ObjectUtil.isEmpty(other.link) &&
                !ObjectUtil.isEmpty(this.link))
            ret = true;

        if (ObjectUtil.isEmpty(other.length) &&
                !ObjectUtil.isEmpty(this.length))
            ret = true;

        if (ObjectUtil.isEmpty(other.image) &&
                !ObjectUtil.isEmpty(this.image))
            ret = true;
        
        return ret;
    }

    /**
     Checks if episode has all non-empty fields.
     */
    public boolean isComplete() {
        boolean ret = true;
        if (id == 0) ret = false;
        if (ObjectUtil.isEmpty(title)) ret = false;
        if (ObjectUtil.isEmpty(description)) ret = false;
        if (ObjectUtil.isEmpty(length)) ret = false;
        if (ObjectUtil.isEmpty(link)) ret = false;
        if (ObjectUtil.isEmpty(image)) ret = false;
        if (ObjectUtil.isEmpty(lastUpdated)) ret = false;
        
        return ret;
    }

    public void completeWith(Episode other) {

        if (!other.completes(this))
            return;

        //this has id
        //this had year
        //this has title
        if (!ObjectUtil.isEmpty(other.description)) this.description = other.description;
        if (!ObjectUtil.isEmpty(other.length)) this.length = other.length;
        if (!ObjectUtil.isEmpty(other.link)) this.link = other.link;
        if (!ObjectUtil.isEmpty(other.image)) this.image = other.image;
        if (!ObjectUtil.isEmpty(other.lastUpdated)) this.lastUpdated = other.lastUpdated;
        //no downloadID
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Episode episode = (Episode) o;
        return id == episode.id &&
                year == episode.year &&
                downloadId == episode.downloadId &&
                title.equals(episode.title) &&
                Objects.equals(description, episode.description) &&
                Objects.equals(length, episode.length) &&
                Objects.equals(link, episode.link) &&
                Objects.equals(image, episode.image) &&
                Objects.equals(lastUpdated, episode.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, year, title, description, length, link, image, lastUpdated, downloadId);
    }

    @Override
    public String toString() {
        return "[ ID: " + id + "; " +
                "Year: " + year + "; " +
                "Title: " + title + "; " +
                "Description: " + description + "; " +
                "Length: " + length + "; " +
                "Link: " + link + "; " +
                "Image: " + image + "; " +
                "LastUpdated: " + lastUpdated + "; " +
                "DownloadID: " + downloadId + " ]";
    }
}
