package com.example.upocarsharing;

public class Dati {

    private String seats;
    private String locdest;
    private String datatime;
    private String user;

    public Dati(String seat, String locdest, String datatime, String user) {
        this.seats = seat;
        this.locdest = locdest;
        this.datatime = datatime;
        this.user = user;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getLocdest() {
        return locdest;
    }

    public void setLocdest(String locdest) {
        this.locdest = locdest;
    }

    public String getDatatime() {
        return datatime;
    }

    public void setDatatime(String datatime) {
        this.datatime = datatime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
