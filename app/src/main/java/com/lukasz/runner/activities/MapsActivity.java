package com.lukasz.runner.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lukasz.runner.R;
import com.lukasz.runner.entities.User;
import com.lukasz.runner.services.GpsService;
import com.lukasz.runner.services.GpsService.CreateBinder;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private DrawerLayout drawerMenu;
    private LinearLayout menu;
    private Button button;
    private GpsService gpsService;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        drawerMenu = (DrawerLayout) findViewById(R.id.drawer_layout);
        menu = (LinearLayout) findViewById(R.id.leftMenuLayout);
        button = (Button) findViewById(R.id.button);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        Intent intent = new Intent(this, GpsService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        Bundle data = getIntent().getExtras();
        user = data.getParcelable("user");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng warsaw = new LatLng(52, 21);
        mMap.addMarker(new MarkerOptions().position(warsaw).title("Marker in Warsaw"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(warsaw));
    }

    //otweira bozne menu i przypisuje activity w service
    public void openMenu(View view) {
        gpsService.setActivity(this);
        drawerMenu.openDrawer(menu);
    }

    //metoda do przycisku "start" z bocznego menu, uruchamia nasłuchiwanie zmian pozycji
    public void startTracking(View view){
        gpsService.startLocationUpdates();
    }

    //metoda do przycisku "stop", zatrzymuje nasłuciwanie pozycji, zamyka menu
    public void stopTracing(View view){
        gpsService.stopLocation();
        drawerMenu.closeDrawer(menu);
    }

    public void displayData(View view){
        SharedPreferences sharedPref = getSharedPreferences("coords", MODE_PRIVATE);
       // System.out.println(sharedPref.getString("data", "@@@ i w pizdu poszedl sie jebac"));
    }

    public void settext(String text){
        button.setText(text);
    }



    //metoda potrzebna do zbindowania service, pobiera instancje serwisu
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GpsService.CreateBinder binder = (CreateBinder) service;
            gpsService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}
