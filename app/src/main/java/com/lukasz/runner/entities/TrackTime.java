package com.lukasz.runner.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lukasz on 2017-11-28.
 */

public class TrackTime implements Parcelable{



    //zachować kolejność w Parcelable
    private User user;
    private Track track;
    private Date date = new Date();
    private String time;
    private List<Double> latitude = new ArrayList<>();
    private List<Double> longitude = new ArrayList<>();

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Track getTrack() {
        return track;
    }
    public void setTrack(Track track) {
        this.track = track;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setLatitude(List<Double> latitude){
        this.latitude = latitude;
    }
    public List<Double> getLatitude(){
        return latitude;
    }
    public void setLongitude(List<Double> longitude){
        this.longitude = longitude;
    }
    public List<Double> getLongitude(){
        return longitude;
    }


    public TrackTime (User user){
        this.user=user;
    }


    public void addCoords(Double lat, Double lng){
        latitude.add(lat);
        longitude.add(lng);
    }


//--------------------------PARCELABLE------------------------

    //zachować kolejnoś z writeToParcel
    public TrackTime(Parcel in){
        final ClassLoader cl = getClass().getClassLoader();
        user = (User) in.readValue(cl);
        track = (Track) in.readValue(cl);
        date = (Date) in.readValue(cl);
        time = in.readString();
        in.readList(latitude, cl);
        in.readList(longitude, cl);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(track);
        dest.writeValue(date);
        dest.writeString(time);
        dest.writeList(latitude);
        dest.writeList(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<TrackTime> CREATOR = new Parcelable.Creator<TrackTime>() {
        public TrackTime createFromParcel(Parcel in) {
            return new TrackTime(in);
        }
        public TrackTime[] newArray(int size) {
            return new TrackTime[size];
        }
    };

}
