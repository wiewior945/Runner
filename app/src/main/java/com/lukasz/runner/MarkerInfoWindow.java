package com.lukasz.runner;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.lukasz.runner.entities.Track;

/**
 * Created by Lukasz on 2017-12-03.
 * interfejs jest używany do ustawiania customowego widoku po kliknięciu na marker na mapie
 */
public class MarkerInfoWindow implements GoogleMap.InfoWindowAdapter {

    private Context context;


    public MarkerInfoWindow(Context context){
        this.context=context;
    }

    // tutaj tworzony jest popup markera, nie można dodawać żadnych przycisków bo to jest wyświetlane jako obrazek
    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = View.inflate(context, R.layout.marker_info_window_layout, null);
        Track track = (Track) marker.getTag();
        TextView title = (TextView) infoWindow.findViewById(R.id.markerInfoWindowTitle);
        title.setText(track.getName());
        TextView distance = (TextView) infoWindow.findViewById(R.id.markerInfoWindowLengthValue);
        distance.setText(Integer.toString(track.getDistance())+"m");
        return infoWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
