package com.personal.project.explora.feed;

import android.util.Log;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.utils.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class FeedDealer {

    private static final String TAG = "FeedDealer";

    private static final String BASE_URL = "https://radio.hrt.hr/";
    private static final String RELATIVE_URL = "podcast/rss/radio-pula/1277/explora.xml/";

    private Channel mChannel;
    private FeedAPI mFeedAPI;

    public FeedDealer() {
        mChannel = new Channel();
        initialize();
    }

   public void initialize() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        mFeedAPI = retrofit.create(FeedAPI.class);

        Call<Rss> rssCall = mFeedAPI.getRss(BASE_URL + RELATIVE_URL);
        rssCall.enqueue(new Callback<Rss>() {
            @Override
            public void onResponse(Call<Rss> call, Response<Rss> response) {
                Log.d(TAG, "onResponse: " + response.code());
                if (!response.isSuccessful() || response.body() == null) {
                    mChannel = new Channel();
                    return;
                }

                mChannel = response.body().getChannel();

                if (mChannel == null)
                    mChannel = new Channel();
            }

            @Override
            public void onFailure(Call<Rss> call, Throwable t) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS - " + t.getMessage());
                mChannel = null;
            }
        });
    }

    public List<Episode> getEpisodes() {
        return mChannel.getEpisodes();
    }

}
