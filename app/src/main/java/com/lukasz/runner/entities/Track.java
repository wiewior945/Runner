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
    private Long id=0L;
    private User user;
    private List<Double> latitude = new ArrayList<>();
    private List<Double> longtitude = new ArrayList<>();
    private Double startLatitude, startLomgtitude;
    private Double endLatitude, endLomgtitude;
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

    public boolean endTrack(){
        try{
            startLatitude = latitude.get(0);
            startLomgtitude = longtitude.get(0);
            endLatitude = latitude.get(latitude.size()-1);
            endLomgtitude = longtitude.get(longtitude.size()-1);
            return true;
        }
        catch(IndexOutOfBoundsException e){
            return false;
        }

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
    public Double getStartLatitude() {return startLatitude;}
    public void setStartLatitude(Double startLatitude) {this.startLatitude = startLatitude;}
    public Double getStartLomgtitude() {return startLomgtitude;}
    public void setStartLomgtitude(Double startLomgtitude) {this.startLomgtitude = startLomgtitude;}
    public Double getEndLatitude() {return endLatitude;}
    public void setEndLatitude(Double endLatitude) {this.endLatitude = endLatitude;}
    public Double getEndLomgtitude() {return endLomgtitude;}
    public void setEndLomgtitude(Double endLomgtitude) {this.endLomgtitude = endLomgtitude;}
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
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    //----------------------  PARCELABLE ------------------------------------
    public Track(Parcel in){ //zachować kolejność z writeToParcel
        final ClassLoader cl = getClass().getClassLoader();
        id=in.readLong();
        user = (User)in.readValue(cl);
        in.readList(latitude, cl);
        in.readList(longtitude, cl);
        startLatitude = in.readDouble();
        startLomgtitude = in.readDouble();
        endLatitude = in.readDouble();
        endLomgtitude = in.readDouble();
        dateCreated = (Date)in.readValue(cl);
        startDescription = in.readString();
        finishDescription = in.readString();
        name = in.readString();
        time = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeValue(user);
        dest.writeList(latitude);
        dest.writeList(longtitude);
        dest.writeDouble(startLatitude);
        dest.writeDouble(startLomgtitude);
        dest.writeDouble(endLatitude);
        dest.writeDouble(endLomgtitude);
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
