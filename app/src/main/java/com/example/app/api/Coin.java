package com.example.app.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Coin implements Serializable {


    @SerializedName("id")
    private String id;
    @SerializedName("symbol")
    private String symbol;
    @SerializedName("name")
    private String name;
    @SerializedName("image")
    private String image;
    @SerializedName("current_price")
    private double currentPrice;
    @SerializedName("market_cap_rank")
    private int marketCapRank;

    @SerializedName("market_data")
    public MarketData market_data;

    public class MarketData {
        @SerializedName("current_price")
        public CurrentPrice current_price;
    }

    public class CurrentPrice {
        @SerializedName("usd")
        public double usd;
    }

    private transient double ownedAmount = 0.0;

    public String getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public double getCurrentPrice() { return currentPrice; }
    public int getMarketCapRank() { return marketCapRank; }
    public double getOwnedAmount() { return ownedAmount; }

    public void setOwnedAmount(double ownedAmount) {
        this.ownedAmount = ownedAmount;
    }
}