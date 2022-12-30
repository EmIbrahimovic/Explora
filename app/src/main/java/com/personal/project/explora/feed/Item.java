package com.personal.project.explora.feed;

import com.personal.project.explora.utils.DateUtil;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.time.LocalDate;

@Root(name = "item", strict =  false)
public class Item implements Serializable {

    @Element(name = "title")
    private final String title;

    @Element(name = "link")
    private final String shareLink;
    private final String link;
    @Element(name = "description", required = false)
    private final String description;
    @Element(name = "pubDate")
    private final String date;
    @Element(name = "duration")
    private final long duration;
    @Element(name = "enclosure")
    private Enclosure enclosure;

    public Item(@Element(name = "title") String title,
                @Element(name = "enclosure") Enclosure enclosure,
                @Element(name = "link") String shareLink,
                @Element(name = "description", required = false) String description,
                @Element(name = "pubDate") String date,
                @Element(name = "duration") long duration) {
        this.title = title;
        this.link = enclosure.url;
        this.shareLink = shareLink;
        this.description = description;
        this.date = date;
        this.duration = duration;
    }

    public String getTitle()
    {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getShareLink() {
        return shareLink;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public long getDuration() {
        return duration;
    }

    public int getYear() {
        LocalDate date = DateUtil.parse(this.getDate());
        return date.getYear();
    }

    @Override
    public String toString() {
        return "Item{" +
                "title='" + title + '\'' +
                ", shareLink='" + shareLink + '\'' +
                ", link='" + link + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", duration=" + duration +
                '}';
    }

    public static class Enclosure {

        @Attribute(name = "url")
        private String url;

        @Attribute(name = "type")
        private String type;

        @Attribute(name = "length")
        private long length;

    }
}
