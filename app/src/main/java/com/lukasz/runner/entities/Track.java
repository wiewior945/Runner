package com.lukasz.runner.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lukasz on 2017-03-15.
 */

public class Track implements Parcelable{

    //wprowadzać zmiany w Parclable!!!
    private User user;
    private List<Double> latitude = new ArrayList<>();
    private List<Double> longtitude = new ArrayList<>();
    private LatLng startPoint, endPoint;
    private Date dateCreated = new Date();
    private String startDescription;
    private String finishDescription;
    private String name;
    private String time;

    public Track(User user){
        this.user=user;
    }


    public void addCoords(Double lat, Double lng){
        latitude.add(lat);
        longtitude.add(lng);
    }

    public void endTrack(){
        startPoint = new LatLng(latitude.get(0), longtitude.get(0));
        endPoint = new LatLng(latitude.get(latitude.size()-1), longtitude.get(longtitude.size()-1));
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
    public void setStartDescription(String description){
        startDescription=description;
    }
    public String getStartDescription(){
        return startDescription;
    }
    public String getFinishDescription() {
        return finishDescription;
    }
    public void setFinishDescription(String finishDescription) {this.finishDescription = finishDescription;}
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }


    //----------------------  PARCELABLE ------------------------------------
    public Track(Parcel in){ //zachować kolejność z writeToParcel
        final ClassLoader cl = getClass().getClassLoader();
        user = (User)in.readValue(cl);
        in.readList(latitude, cl);
        in.readList(longtitude, cl);
        startPoint = (LatLng)in.readValue(cl);
        endPoint = (LatLng)in.readValue(cl);
        dateCreated = (Date)in.readValue(cl);
        startDescription = in.readString();
        finishDescription = in.readString();
        name = in.readString();
        time = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeList(latitude);
        dest.writeList(longtitude);
        dest.writeValue(startPoint);
        dest.writeValue(endPoint);
        dest.writeValue(dateCreated);
        dest.writeString(startDescription);
        dest.writeString(finishDescription);
        dest.writeString(name);
        dest.writeString(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Track> CREATOR
            = new Parcelable.Creator<Track>() {
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        public Track[] newArray(int size) {
            return new Track[size];
        }
    };
    //-----------------------------------------------------------------------
}
