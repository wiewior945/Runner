package com.lukasz.runner.entities;

import android.os.Parcel;
import android.os.Parcelable;

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
    private List<Double> longitude = new ArrayList<>();
    private Double startLatitude, startLongitude;
    private Double endLatitude, endLongitude;
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
        longitude.add(lng);
    }

    public boolean endTrack(){
        try{
            startLatitude = latitude.get(0);
            startLongitude = longitude.get(0);
            endLatitude = latitude.get(latitude.size()-1);
            endLongitude = longitude.get(longitude.size()-1);
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
    public void setLongitude(List<Double> longitude){
        this.longitude = longitude;
    }
    public List<Double> getLongitude(){
        return longitude;
    }
    public void setDateCreated(Date date){
        dateCreated = date;
    }
    public Date getDateCreated(){
        return dateCreated;
    }
    public Double getStartLatitude() {return startLatitude;}
    public void setStartLatitude(Double startLatitude) {this.startLatitude = startLatitude;}
    public Double getStartLongitude() {return startLongitude;}
    public void setStartLongitude(Double startLongitude) {this.startLongitude = startLongitude;}
    public Double getEndLatitude() {return endLatitude;}
    public void setEndLatitude(Double endLatitude) {this.endLatitude = endLatitude;}
    public Double getEndLongitude() {return endLongitude;}
    public void setEndLongitude(Double endLongitude) {this.endLongitude = endLongitude;}
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
        in.readList(longitude, cl);
        startLatitude = in.readDouble();
        startLongitude = in.readDouble();
        endLatitude = in.readDouble();
        endLongitude = in.readDouble();
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
        dest.writeList(longitude);
        dest.writeDouble(startLatitude);
        dest.writeDouble(startLongitude);
        dest.writeDouble(endLatitude);
        dest.writeDouble(endLongitude);
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
