package com.lukasz.runner.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lukasz on 2017-08-30.
 */

public class User implements Parcelable{

    private Long id;
    private String name;
    private String password;


    public User(){};

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public void setPassword(String password){
        this.password = password;
    }
    public String getPassword(){
        return password;
    }

    public void setId(Long id){ this.id = id; }
    public Long getId(){ return id; }



    //-----------  Parcelable  ---------------

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    //Ważna ta sama kolejność zapisywania danych co w poniższym konstruktorze! Każde nowe pole trzeba dodać w tych dwóch miejscach.
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(password);
    }

    private User(Parcel in){
        id = in.readLong();
        name = in.readString();
        password = in.readString();
    }
}
