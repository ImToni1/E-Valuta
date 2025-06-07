package com.example.app.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {

    @SerializedName("Data")
    private List<Article> data;

    public List<Article> getData() {
        return data;
    }
}