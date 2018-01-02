package com.lukasz.runner.entities;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lukasz on 2017-03-15.
 */

public class Track implements Parcelable{

    //jakie kolwiek zmiany pól trzeba dodać w MapsActivity, tam jest mapowanie podczas pobierania markerów
    //wprowadzać zmiany w Parclable!!!
    private Long id=0L;
    private User user;
    private Double startLatitude, startLongitude;
    private Double endLatitude, endLongitude;
    private Date dateCreated;
    private String startDescription;
    private String finishDescription;
    private String name;
    private int distance;

    public Track(){};


    public void calculateDistance(){
        float[] distanceResults = new float[10];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, distanceResults);
        distance = Math.round(distanceResults[0]);
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
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public int getDistance() {return distance;}
    public void setDistance(int distance) {this.distance = distance;}


    //----------------------  PARCELABLE ------------------------------------
    public Track(Parcel in){ //zachować kolejność z writeToParcel
        final ClassLoader cl = getClass().getClassLoader();
        id=in.readLong();
        user = (User)in.readValue(cl);
        startLatitude = in.readDouble();
        startLongitude = in.readDouble();
        endLatitude = in.readDouble();
        endLongitude = in.readDouble();
        dateCreated = (Date)in.readValue(cl);
        startDescription = in.readString();
        finishDescription = in.readString();
        name = in.readString();
        distance = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeValue(user);
        dest.writeDouble(startLatitude);
        dest.writeDouble(startLongitude);
        dest.writeDouble(endLatitude);
        dest.writeDouble(endLongitude);
        dest.writeValue(dateCreated);
        dest.writeString(startDescription);
        dest.writeString(finishDescription);
        dest.writeString(name);
        dest.writeInt(distance);
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
