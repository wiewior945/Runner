package com.lukasz.runner.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Lukasz on 2017-11-28.
 */

public class TrackTime implements Parcelable{

    public TrackTime(User user, Track track, Date date, String time){
        this.user=user;
        this.track=track;
        this.date=date;
        this.time=time;
    }

    //zachować kolejność w Parcelable
    private User user;
    private Track track;
    private Date date;
    private String time;

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



//--------------------------PARCELABLE------------------------

    //zachować kolejnoś z writeToParcel
    public TrackTime(Parcel in){
        final ClassLoader cl = getClass().getClassLoader();
        user = (User) in.readValue(cl);
        track = (Track) in.readValue(cl);
        date = (Date) in.readValue(cl);
        time = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(track);
        dest.writeValue(date);
        dest.writeString(time);
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
