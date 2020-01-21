package com.example.tommy.todolist;

public class Dati {

    private String titolo;
    private String sottotitolo;
    private String autore;
    private String lat;
    private String lng;

    public Dati(String titolo, String sottotitolo, String lat, String lng, String autore) {
        this.titolo = titolo;
        this.sottotitolo = sottotitolo;
        this.autore = autore;
        this.lat = lat;
        this.lng = lng;
    }

    public Dati(String titolo, String sottotitolo, String lat, String lng) {
        this.titolo = titolo;
        this.sottotitolo = sottotitolo;
        this.lat = lat;
        this.lng = lng;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getSottotitolo() {
        return sottotitolo;
    }

    public void setSottotitolo(String sottotitolo) {
        this.sottotitolo = sottotitolo;
    }

    public String getAutore() {
        return autore;
    }

    public void setAutore(String autore) {
        this.autore = autore;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
