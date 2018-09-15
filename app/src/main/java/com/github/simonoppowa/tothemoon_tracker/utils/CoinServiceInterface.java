package com.github.simonoppowa.tothemoon_tracker.utils;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CoinServiceInterface {

    @GET("top/exchanges/full")
    Call<JsonElement> getCoinInfo(@Query("fsym") String coinName, @Query("tsym") String currencyName);

    @GET("price")
    Call<JsonElement> getSingleCoinPrice(@Query("fsym") String coinName, @Query("tsyms") String currency);
}