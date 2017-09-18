package com.lukasz.runner.entities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lukasz on 2017-03-15.
 */

public class Track {

    private double longtitude, langtitude;
    private String date;

    public Track(double longtitude, double latitude){
        this.longtitude = longtitude;
        this.langtitude = latitude;
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        this.date = format.format(date);
    }

    public void setLongtitude(double x){
        this.longtitude = x;
    }

    public void setlatitude(double x){
        this.langtitude = x;
    }

    public double getLongtitude(){
        return longtitude;
    }

    public double getlatitude(){
        return langtitude;
    }

    public String getDate(){
        return date;
    }
}
