package com.personal.project.explora.feed;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.utils.DateUtil;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.time.LocalDate;

@Root(name = "item", strict =  false)
public class Item implements Serializable {

    @Element(name = "title")
    private String title;

    @Element(name = "link")
    private String link;

    @Element(name = "description", required = false)
    private String description;

    @Element(name = "pubDate")
    private String date;

    public Item(@Element(name = "title") String title,
                @Element(name = "link") String link,
                @Element(name = "description", required = false) String description,
                @Element(name = "pubDate") String date) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.date = date;
    }

    public String getTitle()
    {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    /*
    Uses Episode(String link, String description, LocalDate date, LocalDate lastUpdated).
    Title is made by the constructor, and rest of the fields are null.
     */
    public Episode toEpisode(LocalDate lastUpdated) {
        return new Episode(
                this.getTitle(),
                this.getLink(),
                this.getDescription(),
                DateUtil.parse(this.getDate()),
                lastUpdated);
    }

}
