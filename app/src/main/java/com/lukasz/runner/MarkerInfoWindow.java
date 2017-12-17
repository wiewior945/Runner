package com.lukasz.runner;

import android.content.Context;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Lukasz on 2017-12-03.
 * interfejs jest używany do ustawiania customowego widoku po kliknięciu na marker na mapie
 */
public class MarkerInfoWindow implements GoogleMap.InfoWindowAdapter {

    private Context context;


    public MarkerInfoWindow(Context context){
        this.context=context;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = View.inflate(context, R.layout.marker_info_window_layout, null);
        return infoWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
