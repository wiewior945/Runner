package com.lukasz.runner.entities;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Lukasz on 2017-11-15.
 */

public class MarkerInfo {

    private long trackId;
    private String trackName;
    private LatLng position;


    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }
}
