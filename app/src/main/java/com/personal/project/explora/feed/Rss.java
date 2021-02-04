package com.personal.project.explora.feed;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

@Root(name = "rss", strict =  false)
public class Rss implements Serializable {

    @Element(name = "channel")
    private Channel channel;

    public Rss(@Element(name = "channel") Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

}
