package com.lukasz.runner.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.lukasz.runner.Utilities;
import com.lukasz.runner.com.lukasz.runner.dialogs.InfoDialog;
import com.lukasz.runner.entities.Track;
import com.lukasz.runner.activities.MapsActivity;
import com.lukasz.runner.entities.User;

/**
 * Created by Lukasz on 2017-03-10.
 */

public class GpsService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final int GPS_ENABLE_DIALOG_REQUESTCODE = 1337;

    private Binder binder = new CreateBinder();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    private MapsActivity mapsActivity;
    private Track track;
    private boolean recordTrack = false;


    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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

    //metoda potrzebna do zbindowania serwisu, tworzy obkiet Binder z metodą do pobrania instancji serwisu
    public class CreateBinder extends Binder {
        public GpsService getService() {
            return GpsService.this;
        }
    }

    public void setActivity(MapsActivity maps){
        mapsActivity = maps;
    }


    //sprawdza czy aplikacja ma uprawnienia do GPS, potem sprawdza czy GPS jest włączony, na koniec uruchamia LocationListener
    public void startLocationListening() {
        final GpsService gps = this;
        if(locationRequest == null){
            locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(3000);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Utilities.requestGpsPermiossions(mapsActivity);
        }
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:   //GPS aktywny, włączenie LocationListener
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, gps);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:   //GPS wyłączony, okienko z włączeniem
                        try{
                            status.startResolutionForResult(mapsActivity, GPS_ENABLE_DIALOG_REQUESTCODE);
                        }catch (IntentSender.SendIntentException e){

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:   //do końca nie wiem, chyba brak GPS w urządzeniu
                        InfoDialog.showOkDialog(mapsActivity, "Nie można aktywować GPS");
                        break;
                }
            }
        });
    }


//    //wyłącza nasłuchiwanie zmian lokacji, zapisuje trasę
//    public void stopLocation(){
//        mGoogleApiClient.disconnect();
//        SharedPreferences sharedPref = getSharedPreferences("coords", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString("data", temporaryData);
//        editor.commit();
//    }

    public void newTrack(User user){
        track = new Track(user);
    }

    //jeśli nie zapisani żadnego punktu (zmiany położenia) nie ma co zapisywac trasy i zwra nulla
    public Track saveTrack(){
        recordTrack=false;
        Track tempTrack;
        if(track.endTrack()){
            tempTrack = track;
        }else{
            tempTrack=null;
            InfoDialog.showOkDialog(mapsActivity, "Nie zanotowano żadnej zmiany położenia. Sprawdź ustawienia GPS lub rusz tyłek!.");
        }
        track = null;
        return tempTrack;
    }

    public void cancelTrack(){
        recordTrack=false;
        track=null;
    }

    @Override
    public void onLocationChanged(Location location) {
        mapsActivity.setGpsFlag(true);
        mapsActivity.centerMap(location.getLatitude(), location.getLongitude());
        if(recordTrack){
            track.addCoords(location.getLatitude(), location.getLongitude());
        }
        System.out.println("@@@   "+location.getLatitude()+"   "+location.getLongitude());
    }


    public void setRecordTrack(boolean bool){
        recordTrack=bool;
    }
    public boolean isRecordTrack() {
        return recordTrack;
    }

    private class GpsChecking implements Runnable{

        @Override
        public void run() {

        }
    }
}
