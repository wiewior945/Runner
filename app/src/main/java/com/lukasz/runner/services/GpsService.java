package com.lukasz.runner.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.lukasz.runner.entities.Track;
import com.lukasz.runner.activities.MapsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lukasz on 2017-03-10.
 */

public class GpsService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Binder binder = new CreateBinder();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private List<Track> coordsList = new ArrayList<Track>();
    private MapsActivity mapsActivity;

    private String temporaryData = "";

    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //zaczyna nasłuchiwanie zmian pozycji, ------------ TRZEBA TEGO IFA OGARNĄĆ ----------------
    public void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
           // return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    //metoda potrzebna do zbindowania serwisu, tworzy obkiet Binder z metodą do pobrania instancji serwisu
    public class CreateBinder extends Binder {
        public GpsService getService() {
            return GpsService.this;
        }
    }

    public void setActivity(MapsActivity maps){
        mapsActivity = maps;
    }

    //wyłącza nasłuchiwanie zmian lokacji, zapisuje trasę
    public void stopLocation(){
        mGoogleApiClient.disconnect();
        SharedPreferences sharedPref = getSharedPreferences("coords", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("data", temporaryData);
        editor.commit();
    }

    @Override
    public void onLocationChanged(Location location) {
        Track coords = new Track(location.getLongitude(), location.getLatitude());
        temporaryData += (coords.getLongtitude()+ "_" +coords.getlatitude()+"_"+ coords.getDate()+";");
    }
}
