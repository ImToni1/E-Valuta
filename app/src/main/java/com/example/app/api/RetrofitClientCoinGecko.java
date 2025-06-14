package com.example.app.api; // Vaš paket

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientCoinGecko {

    private static final String BASE_URL = "https://api.coingecko.com/api/v3/";
    private static Retrofit retrofit = null;

    public static ApiServiceCoinGecko getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiServiceCoinGecko.class);
    }
}