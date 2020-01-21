package com.example.museumguide;

public class Dati {

    private String title;
    private String city;
    private String street;
    private String num;
    private boolean isPhoto;

    public Dati(String title, String city, String street, String num, boolean isPhoto) {
        this.title = title;
        this.city = city;
        this.street = street;
        this.num = num;
        this.isPhoto = isPhoto;
    }

    public boolean isPhoto() {
        return isPhoto;
    }

    public void setPhoto(boolean photo) {
        isPhoto = photo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}
