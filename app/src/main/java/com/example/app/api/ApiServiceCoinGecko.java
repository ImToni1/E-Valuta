package com.example.app.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiServiceCoinGecko {

    @GET("coins/markets")
    Call<List<Coin>> getMarketData(
            @Query("vs_currency") String vsCurrency,
            @Query("order") String order,
            @Query("per_page") int perPage,
            @Query("page") int page,
            @Query("sparkline") boolean sparkline
    );

    @GET("coins/markets")
    Call<List<Coin>> getCoins(
            @Query("vs_currency") String vsCurrency,
            @Query("ids") String ids
    );
}