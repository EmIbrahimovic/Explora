package com.personal.project.explora.feed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface FeedAPI {

    @GET
    Call<Rss> getRss(@Url String url);

}
