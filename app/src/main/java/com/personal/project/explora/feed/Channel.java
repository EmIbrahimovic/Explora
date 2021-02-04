package com.personal.project.explora.feed;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.utils.DateUtil;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Root(name = "channel", strict =  false)
public class Channel implements Serializable {

    @Element(name = "lastBuildDate")
    private String lastBuildDate;

    @ElementList(inline = true, name = "item")
    private List<Item> items;

    public Channel() {
        this.lastBuildDate = "";
        this.items = new ArrayList<>();
    }

    public Channel(@Element(name = "lastBuildDate") String lastBuildDate,
                   @ElementList(inline = true, name = "item") List<Item> items) {
        this.lastBuildDate = lastBuildDate;
        this.items = items;
    }

    public String getLastBuildDate() {
        return lastBuildDate;
    }

    public LocalDate getLastBuildLocalDate() {
        return DateUtil.parse(this.getLastBuildDate());
    }

    public List<Item> getItems() {
        return items;
    }

    /**
     * The returned episodes will be of format:
     * Episode(id=0, year, title, description(may be null), lenght=null, link, image=null, lastUpd)
     * @return list of retrieved episodes
     */
    public List<Episode> getEpisodes() {
        return convertItemsToEpisodes(this.getItems());
    }

    private List<Episode> convertItemsToEpisodes(List<Item> items) {

        List<Episode> episodes = new ArrayList<>();
        for (Item item : items) {
            episodes.add(item.toEpisode(getLastBuildLocalDate()));
        }

        return episodes;
    }

}
