package com.personal.project.explora.db;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.personal.project.explora.utils.DateUtil;
import com.personal.project.explora.utils.StringUtils;

import java.time.LocalDate;
import java.util.Objects;

@Entity(tableName = "episodes_table")
public class Episode {

    public static final int NOT_DOWNLOADED = 0;
    public static final int DOWNLOADING = 1;
    public static final int DOWNLOADED = 2;
    private static final int COMPLETED_PERCENTAGE_THRESHOLD = 97;

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int year;
    private String title;
    private String description;
    private String link;
    private String datePublished;

    @ColumnInfo(name = "downloadId")
    private int downloadState;
    private String lastUpdated;
    private long lastPosition;
    private long duration;
    private String recent;

    public Episode(int year,
                   String title,
                   String description,
                   String link,
                   String datePublished,
                   String lastUpdated,
                   long duration) {

        this.year = year;
        this.title = title;
        this.description = description;
        this.link = link;
        this.datePublished = datePublished;
        this.downloadState = NOT_DOWNLOADED;
        this.lastUpdated = lastUpdated;
        this.lastPosition = 0L;
        this.duration = duration;
        this.recent = null;
    }

    public Episode(Episode other) {
        this.id = other.id;
        this.year = other.year;
        this.title = other.title;
        this.description = other.description;
        this.link = other.link;
        this.datePublished = other.datePublished;
        this.downloadState = other.downloadState;
        this.lastUpdated = other.lastUpdated;
        this.lastPosition = other.lastPosition;
        this.duration = other.duration;
        this.recent = other.recent;
    }

    /**
     * Duration should never be not set, but the only time I use this method I call setDuration a
     * bit later on (BAD CODE I KNOW)
     */
    public Episode(String title, String link, String description, LocalDate date, LocalDate lastUpdated) {
        this(date.getYear(),
                title,
                description,
                link,
                DateUtil.formatMyDate(date),
                DateUtil.formatMyDate(lastUpdated),
                0L);
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

    public String getLink() {
        return link;
    }

    public String getDatePublished() { return datePublished; }

    public static boolean isValidDownloadId(int downloadId) {
        return downloadId == DOWNLOADED || downloadId == NOT_DOWNLOADED || downloadId == DOWNLOADING;
    }

    public Uri getUri() {
        return Uri.parse(link);
    }

    public int getDownloadState() {
        return downloadState;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
    }

    public long getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(long lastPosition) {
        this.lastPosition = lastPosition;
    }

    public void markAsCompleted() {
        this.lastPosition = duration;
    }

    public void resetProgress() {
        this.lastPosition = 0L;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getRecent() {
        return recent;
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    public boolean isCompleted() {
        return (100 * lastPosition/duration >= COMPLETED_PERCENTAGE_THRESHOLD);
    }

    /**
     Checks if other episode has some non-null or non-empty attributes which this episode has.
     Only performs check if episode titles match.
     Episode which is being completed must have id and title (by extension also year).
     */
    public boolean completes(Episode other) {
        if (other.areContentsComplete())
            return false;
        
        if (!this.title.equals(other.title))
            return false;
        
        boolean ret = false;
        if (StringUtils.isEmpty(other.description) &&
                !StringUtils.isEmpty(this.description))
            ret = true;

        if (StringUtils.isEmpty(other.link) &&
                !StringUtils.isEmpty(this.link))
            ret = true;
        
        return ret;
    }

    /**
     Checks if episode has all non-empty important fields.
     */
    public boolean areContentsComplete() {
        boolean ret = true;
        if (id <= 0) ret = false;
        if (StringUtils.isEmpty(title)) ret = false;
        if (StringUtils.isEmpty(description)) ret = false;
        if (StringUtils.isEmpty(link)) ret = false;
        
        return ret;
    }

    /**
     * used in when I'm updating the database with freshly-baked episodes from the RSS. these
     * fresh pastry dudes will not have a downloadId or lastPosition or be recent so there is no
     * need to update those, since if the episode already exists in my database it might have
     * these values set to something meaningful that is not worth resetting
     */
    public void completeContentWith(Episode other) {

        if (!other.completes(this))
            return;

        //this has id
        //this had year
        //this has title
        if (!StringUtils.isEmpty(other.description)) this.description = other.description;
        if (!StringUtils.isEmpty(other.link)) this.link = other.link;
        //this has date
        if (!StringUtils.isEmpty(other.lastUpdated)) this.lastUpdated = other.lastUpdated;
        //no lastposition
        //no downloadID
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Episode episode = (Episode) o;
        return id == episode.id &&
                year == episode.year &&
                title.equals(episode.title) &&
                Objects.equals(description, episode.description) &&
                Objects.equals(link, episode.link) &&
                Objects.equals(datePublished, episode.datePublished) &&
                downloadState == episode.downloadState &&
                Objects.equals(lastUpdated, episode.lastUpdated) &&
                lastPosition == episode.lastPosition &&
                duration == episode.duration &&
                Objects.equals(recent, episode.recent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, year, title, description, link, datePublished, downloadState,
                lastUpdated, lastPosition, duration, recent);
    }

    @NonNull
    @Override
    public String toString() {
        return "[ ID: " + id + "; " +
                "Title: " + title + "; " +
                "Date Published: " + datePublished + "; " +
                "DownloadId: " + downloadState + "; " +
                "LastPosition: " + lastPosition + "; " +
                "Duration: " + duration + "; " +
                "Recent: " + recent + " ]";
    }

}
