package com.example.app.api;

import com.google.gson.annotations.SerializedName;


public class Article {

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}