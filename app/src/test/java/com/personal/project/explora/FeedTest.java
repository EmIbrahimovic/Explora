package com.personal.project.explora;

import android.util.Log;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.feed.Channel;
import com.personal.project.explora.feed.FeedAPI;
import com.personal.project.explora.feed.Rss;
import com.personal.project.explora.utils.DateUtil;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Znam da test ne valja, suti
 */
public class FeedTest {

    private static final String TAG = "FeedTest";
    public static final String BASE_URL =
            "https://radio.hrt.hr/";
    Channel mChannel;


    @Before
    public void setUp() throws IOException {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        FeedAPI feedAPI = retrofit.create(FeedAPI.class);

        Call<Rss> rssCall = feedAPI.getRss(BASE_URL + "podcast/rss/radio-pula/1277/explora.xml");

        Response<Rss> response = rssCall.execute();
        System.out.println("onResponse: " + response.code());
        if (!response.isSuccessful() || response.body() == null) {
            mChannel = new Channel();
            return;
        }

        mChannel = response.body().getChannel();
        if (mChannel == null)
            mChannel = new Channel();
    }

    @Test
    public void testChannelNotNull() {
        assertNotNull(mChannel);
        assertNotNull(mChannel.getLastBuildDate());
        assertNotNull(mChannel.getItems());
    }

    @Test
    public void testDate() {
        LocalDate expectedDate = DateUtil.parse("Tue, 26 Jan 2021 17:05:00 +0100");
        assertEquals(expectedDate, mChannel.getLastBuildLocalDate());
        System.out.println("Date: " + mChannel.getLastBuildDate());
    }

    @Test
    public void testEpisodes() {
        List<Episode> episodes = mChannel.getEpisodes();
        assertEquals(episodes.size(), 39);

        System.out.println(episodes);
    }

}