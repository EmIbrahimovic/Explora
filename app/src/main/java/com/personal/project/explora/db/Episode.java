package com.personal.project.explora.db;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.personal.project.explora.utils.StringUtils;

import java.util.Objects;

@Entity(tableName = DatabaseConstants.EPISODE_TABLE_NAME)
public class Episode {

    private static final String EPISODE = "Emisija";

    public static final int NOT_DOWNLOADED = 0;
    public static final int DOWNLOADING = 1;
    public static final int DOWNLOADED = 2;
    private static final int COMPLETED_PERCENTAGE_THRESHOLD = 97;

    @PrimaryKey(autoGenerate = true)
    private int id;
    private final int year;
    private String description;
    private String link;
    private final String datePublished;
    private final long duration;
    private String shareLink;
    private long lastPosition;
//    private boolean nonPlayable;
    @ColumnInfo(name = "downloadId")
    private int downloadState;
    private String recent;

    public Episode(int year,
                   String description,
                   String link,
                   String shareLink,
                   String datePublished,
                   long duration) {

        this.year = year;
        this.description = description;
        this.link = link;
        this.shareLink = shareLink;
        this.datePublished = datePublished;
//        this.nonPlayable = false;
        this.downloadState = NOT_DOWNLOADED;
        this.lastPosition = 0L;
        this.duration = duration;
        this.recent = null;
    }

    public Episode(Episode other) {
        this.id = other.id;
        this.year = other.year;
        this.description = other.description;
        this.link = other.link;
        this.shareLink = other.shareLink;
        this.datePublished = other.datePublished;
//        this.nonPlayable = other.nonPlayable;
        this.downloadState = other.downloadState;
        this.lastPosition = other.lastPosition;
        this.duration = other.duration;
        this.recent = other.recent;
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
        return EPISODE + " " + datePublished;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getShareLink() {
        return shareLink;
    }

    public String getDatePublished() { return datePublished; }

//    public boolean isNonPlayable() {
//        return nonPlayable;
//    }
//
//    public void setNonPlayable(boolean nonPlayable) {
//        if (!(nonPlayable && this.downloadState == DOWNLOADED))
//            this.nonPlayable = nonPlayable;
//    }

    public static boolean isValidDownloadId(int downloadId) {
        return downloadId == DOWNLOADED || downloadId == NOT_DOWNLOADED || downloadId == DOWNLOADING;
    }

    public Uri getUri() {
        return Uri.parse(link);
    }

    public int getDownloadState() {
        return downloadState;
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

    /*public void setDuration(long duration) {
        this.duration = duration;
    }*/

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
        
        if (!this.datePublished.equals(other.datePublished))
            return false;
        
        boolean ret = StringUtils.isEmpty(other.description) &&
                !StringUtils.isEmpty(this.description);

        if (StringUtils.isEmpty(other.link) &&
                !StringUtils.isEmpty(this.link))
            ret = true;

        if (StringUtils.isEmpty(other.shareLink) &&
                !StringUtils.isEmpty(this.shareLink))
            ret = true;

//        if (this.nonPlayable != other.nonPlayable)
//            ret = true;
        
        return ret;
    }

    /**
     Checks if episode has all non-empty important fields.
     */
    public boolean areContentsComplete() {
        boolean ret = id > 0;
        if (StringUtils.isEmpty(datePublished)) ret = false;
        if (StringUtils.isEmpty(description)) ret = false;
        if (StringUtils.isEmpty(link)) ret = false;
        if (StringUtils.isEmpty(shareLink)) ret = false;
        
        return ret;
    }

    /**
     * used in when I'm updating the database with freshly-baked episodes from the RSS. these
     * fresh pastry dudes will not have a downloadId or lastPosition or be recent so there is no
     * need to update those, since if the episode already exists in my database it might have
     * these values set to something meaningful that is not worth resetting
     */
    public void completeContentWith(Episode other) {

        // TODO de ga popravi

        if (!other.completes(this))
            return;

        //this has id
        //this had year
        //this has title
        if (!StringUtils.isEmpty(other.description)) this.description = other.description;
        if (!StringUtils.isEmpty(other.link)) this.link = other.link;
        if (!StringUtils.isEmpty(other.shareLink)) this.shareLink = other.shareLink;
//        this.setNonPlayable(other.nonPlayable);
        //this has date
        //no lastposition
        //no downloadID
    }

    /**
     * Takes in an episode title of the form "Emisija DD.MM.YYYY." and returns "DD.MM.YYYY."
     * @param title Title of the form "Emisija DD.MM.YYYY."
     * @return "DD.MM.YYYY." retrieved from title
     */
    public static String titleToDatePublished(String title)
    {
        int start = 8;
        return title.substring(start, start + 10) + ".";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Episode episode = (Episode) o;
        return id == episode.id &&
                year == episode.year &&
//                nonPlayable == episode.nonPlayable &&
                downloadState == episode.downloadState &&
                lastPosition == episode.lastPosition &&
                duration == episode.duration &&
                Objects.equals(description, episode.description) &&
                Objects.equals(link, episode.link) &&
                Objects.equals(shareLink, episode.shareLink) &&
                datePublished.equals(episode.datePublished) &&
                Objects.equals(recent, episode.recent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, year, description, link, shareLink, datePublished,
                /*nonPlayable,*/ downloadState, lastPosition, duration, recent);
    }

    @Override
    public String toString() {
        return "Episode{" +
                "id=" + id +
                ", year=" + year +
                ", link='" + link + '\'' +
                ", shareLink='" + shareLink + '\'' +
                ", datePublished='" + datePublished + '\'' +
                /*", nonplayable=" + nonPlayable +*/
                ", downloadState=" + downloadState +
                ", lastPosition=" + lastPosition +
                ", duration=" + duration +
                ", recent='" + recent + '\'' +
                '}';
    }

}
