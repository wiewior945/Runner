package com.lukasz.runner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Lukasz on 2017-09-19.
 */

public class Utilities {

    public static void checkInternetPermissions(Activity context){
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.INTERNET}, 123);
        }
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 124);
        }
    }
}
