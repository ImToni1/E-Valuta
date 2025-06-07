package com.example.app.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("data/v2/news/?lang=EN")
    Call<NewsResponse> getNews();
}