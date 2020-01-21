package com.example.booksearch;

import android.graphics.Bitmap;

public class Dati {

    private String title;
    private String lng;
    private String lat;
    private String user;
    private Bitmap imageResource;

    public Dati(String title, String lng, String lat, String user,Bitmap imageResource) {
        this.title = title;
        this.lng = lng;
        this.lat = lat;
        this.user = user;
        this.imageResource = imageResource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Bitmap getImageResource() {
        return imageResource;
    }

    public void setImageResource(Bitmap imageResource) {
        this.imageResource = imageResource;
    }
}
