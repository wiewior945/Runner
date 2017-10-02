package com.lukasz.runner.entities;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lukasz on 2017-03-15.
 */

public class Track {

    private User user;
    private List<Double> latitude = new ArrayList<>();
    private List<Double> longtitude = new ArrayList<>();
    private LatLng startPoint, endPoint;
    private Date dateCreated = new Date();

    public Track(User user){
        this.user=user;
    }


    public void addCoords(Double lat, Double lng){
        latitude.add(lat);
        longtitude.add(lng);
    }



    public void setLatitude(List<Double> latitude){
        this.latitude = latitude;
    }
    public List<Double> getLatitude(){
        return latitude;
    }
    public void setLongtitude(List<Double> longtitude){
        this.longtitude = longtitude;
    }
    public List<Double> getLongtitude(){
        return longtitude;
    }
    public void setDateCreated(Date date){
        dateCreated = date;
    }
    public Date getDateCreated(){
        return dateCreated;
    }
    public void setStartPoint(LatLng point){
        startPoint = point;
    }
    public LatLng getStartPoint(){
        return startPoint;
    }
    public void setEndPoint(LatLng point){
        endPoint = point;
    }
    public LatLng getEndPoint(){
        return endPoint;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public User getUser() {
        return user;
    }
}
