package com.lukasz.runner.activities;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.INotificationSideChannel;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lukasz.runner.R;
import com.lukasz.runner.com.lukasz.runner.dialogs.InfoDialog;
import com.lukasz.runner.entities.MarkerInfo;
import com.lukasz.runner.entities.Track;
import com.lukasz.runner.entities.User;
import com.lukasz.runner.services.GpsService;
import com.lukasz.runner.services.GpsService.CreateBinder;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener{

    private GoogleMap map;
    private DrawerLayout drawerMenu;
    private LinearLayout menu;
    private Button newTrackButton, cancelTrackButton, endTruckTextButton;
    private ImageView endTrackButton, centerMapButton;
    private TextView timerTextView;
    private GpsService gpsService;
    private User user;
    private Handler handler = new Handler();
    private Runnable trackTimer;
    private List<MarkerInfo> currentlyVisibleMarkers = new ArrayList<>();
    private boolean trackingFlag=true;  //czy mapa ma podążać za znacznikiem (obecnym  miejscem)
    private boolean gpsFlag=false;      //czy LocationListener jest uruchomiony

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        drawerMenu=(DrawerLayout) findViewById(R.id.drawer_layout);
        menu=(LinearLayout) findViewById(R.id.leftMenuLayout);
        newTrackButton=(Button) findViewById(R.id.newTrackButton);
        cancelTrackButton=(Button) findViewById(R.id.cancelTrackingButton);
        endTruckTextButton=(Button)findViewById(R.id.endTrackingTextButton);
        endTrackButton=(ImageView) findViewById(R.id.endTrackingButton);
        timerTextView=(TextView) findViewById(R.id.timerTextView);
        centerMapButton=(ImageView) findViewById(R.id.gpsCenterButtosn);
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        Intent intent = new Intent(this, GpsService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        Bundle data = getIntent().getExtras();
        user = data.getParcelable("user");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng warsaw = new LatLng(52.23, 21.006);
        map = googleMap;
        map.setOnCameraIdleListener(this);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false); //ukrywa domyślny przycisk googla do namierzania obecnej lokalizacji
        map.animateCamera(CameraUpdateFactory.newLatLng(warsaw));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onResume(){
        super.onResume();
//        if(gpsService!=null){ // przy starcie aplikacji gpsService jest nullem. To uruchamia listener gdy podczss działania aplikacji ktoś wyłączy gps
//            gpsService.startLocationListening();
//        }
    }


    //metoda potrzebna do zbindowania service, pobiera instancje serwisu
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GpsService.CreateBinder binder = (CreateBinder) service;
            gpsService = binder.getService();
            gpsService.setActivity(getActiviy());
            gpsService.startLocationListening();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    //liestener obsługujący poruszanie się mapy, ładuje trasy gdy mapa zostanie przesunięta
    @Override
    public void onCameraIdle() {
            LatLng rightBottom = map.getProjection().getVisibleRegion().nearRight;
            LatLng leftTop = map.getProjection().getVisibleRegion().farLeft;
            new UpdateTracksOnMap().execute(leftTop,rightBottom);
    }

    //=========================================  PRZYCISKI  =========================================================

    //otweira boczne menu i przypisuje activity w service
    public void openMenu(View view) {
        drawerMenu.openDrawer(menu);
    }

    //metoda do przycisku "nowa trasa"
    public void newTrack(View view){
        drawerMenu.closeDrawer(Gravity.LEFT);
        newTrackButton.setEnabled(false);
        newTrackButton.setTextColor(ContextCompat.getColor(this, R.color.grey));
        cancelTrackButton.setVisibility(View.VISIBLE);
        endTruckTextButton.setVisibility(View.VISIBLE);
        endTrackButton.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.VISIBLE);
        handler.post(new CountDown());
        gpsService.newTrack(user);
    }

    //metoda do przycisku "stop", zatrzymuje zegar, zamyka menu, resetuje trasę w gpsService, przekazuje trasę do Activity zapisu trasy
    //jeśli nie wykryto zmiany pozycji (telefon stał w miejscu albo brak sygnału gps) to service zwraca nulla. Taka trasa nie jest zapisywana.
    public void stopTracking(View view){
        handler.removeCallbacks(trackTimer);
        drawerMenu.closeDrawer(Gravity.LEFT);
        hideTrackButtons();
        Track track = gpsService.saveTrack();
        if(track!=null){                        // jeśli jest nullem w GPS service wyświetlany jest komunikat o braku ruchu
            track.setTime(timerTextView.getText().toString());
            Intent intent = new Intent(this, SaveTrackActivity.class);
            intent.putExtra("track", track);
            startActivity(intent);
        }
        timerTextView.setText("00:00");
    }

    /*
        Metoda do przycisku anulowania trasy. Wyświetla dialog i obsługuje przyciski. W przycisku w metodzie getTag() jest referencja do dialogu.
     */
    public void cancelTracing(View view){
        InfoDialog.showCancelDialog(this, getResources().getText(R.string.confimation_cancel_track).toString(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==(R.id.cancelDialogCancelButton)){ //CANCEL
                    Dialog d = (Dialog) v.getTag();
                    d.dismiss();
                }
                else if(v.getId()==R.id.cancelDialogOkButton){  //OK
                    handler.removeCallbacks(trackTimer);
                    hideTrackButtons();
                    timerTextView.setText("00:00");
                    gpsService.cancelTrack();
                    drawerMenu.closeDrawer(Gravity.LEFT);
                    Dialog d = (Dialog) v.getTag();
                    d.dismiss();
                }
            }
        });
    }

    //zmienia flagę czy mapa powinna podążać za znacznikiem, urachmia ListenerLocation jeśli jeszcze nie jest uruchomiony, zmienie kolor przycisku
    public void trackingButton(View view){
        if(gpsFlag){
            trackingFlag=!trackingFlag;
            if(trackingFlag)centerMapButton.setBackground(ContextCompat.getDrawable(this, R.drawable.gps_icon));
            else centerMapButton.setBackground(ContextCompat.getDrawable(this, R.drawable.grey_gps_icon));
        }
        if(!gpsFlag){
            gpsService.startLocationListening();
        }
    }
    //==================================================================================================================

    private void hideTrackButtons(){
        newTrackButton.setEnabled(true);
        newTrackButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        cancelTrackButton.setVisibility(View.GONE);
        endTruckTextButton.setVisibility(View.GONE);
        endTrackButton.setVisibility(View.GONE);
        timerTextView.setVisibility(View.GONE);
    }

    /*
        Metoda do obsługi systemowego dialogu aktywującego GPS. If to wciśnięcie przycisku ok, else - anuluj.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GpsService.GPS_ENABLE_DIALOG_REQUESTCODE){
            if(resultCode == RESULT_OK){
                gpsService.startLocationListening();
            }
            else{
                InfoDialog.showOkDialog(this, "Bez aktywnego GPS aplikacja nie będzie działać poprawnie.");
            }
        }
    }

    //wywoływana w LocationListener, w zależności od flagi mapa jest centrowana na znaczniku usera lub nie
    public void centerMap(double latitude, double longtitude){
        if(trackingFlag){
            LatLng coords = new LatLng(latitude, longtitude);
            CameraUpdate updatedLocation = CameraUpdateFactory.newLatLng(coords);
            map.animateCamera(updatedLocation);
        }
    }

    //=============================================  TIMERY  ===========================================================
    private class CountDown implements Runnable{
        private int step=3;
        private Dialog dialog;
        @Override
        public void run() {
            if(dialog!=null){
                dialog.dismiss();
            }
            switch (step){
                case 3:
                    dialog = InfoDialog.showNoButtonDialog(getActiviy(), "Gotowy?");
                    break;
                case 2:
                    dialog = InfoDialog.showNoButtonDialog(getActiviy(), "Do startu!");
                    break;
                case 1:
                    dialog = InfoDialog.showNoButtonDialog(getActiviy(), "Start!");
                    break;
            }
            if(step>=0){
                handler.postDelayed(this, 1000);
                step--;
            }
            else{
                handler.post(trackTimer = new TrackTimer());
            }
        }
    }

    private class TrackTimer implements Runnable{
        private int minutes=0;
        private int seconds=0;

        @Override
        public void run() {
            gpsService.setRecordTrack(true);
            seconds++;
            if(seconds==60){
                minutes++;
                seconds=0;
            }
            String time = String.format("%02d:%02d",minutes,seconds);
            timerTextView.setText(time);
            handler.postDelayed(this, 1000);
        }
    }
    //==================================================================================================================


    public void setGpsFlag(boolean b){
        gpsFlag = b;
    }
    public boolean getGpsFlag(){
        return gpsFlag;
    }
    private MapsActivity getActiviy(){
        return this;
    }



//---------------------------------------------------------------------------------------------------------------------------
    //pobiera pozycję znaczników na mapie dla widocznego obszaru i dodaje je na mapę
    private class UpdateTracksOnMap extends AsyncTask<LatLng, Void, List<MarkerInfo>>{

        @Override
        protected List<MarkerInfo> doInBackground(LatLng... corners) {
            String url = getString(R.string.server)+getString(R.string.ws_update_track_markers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            /*  serwer zwraca listę HashMap, nie znalazłem metody do mapowania takich danych do obiektów
             *  dlatego ta pętla leci po wszystkich danych z serwera i tworzy z nich ArrayListę obiektów
             */
            List<LinkedHashMap> json = (List<LinkedHashMap>) restTemplate.postForObject(url, corners, List.class);
            ArrayList<MarkerInfo> markers = new ArrayList<>();
            for(LinkedHashMap<String, Object>data:json){
                MarkerInfo a =new MarkerInfo();
                a.setTrackId(new Long((Integer)data.get("trackId")));
                a.setTrackName((String)data.get("trackName"));
                LinkedHashMap<String, Double> position = (LinkedHashMap)data.get("position");
                a.setPosition(new LatLng(position.get("latitude"), position.get("longitude")));
                markers.add(a);
            }
            return markers;
        }

        /*
            po zakończeniu pobierania danych z serwera wykonuje się ta metoda, jej parametrem jest lista z danymi
            metoda porównuje znaczniki z serwera z tymi obecnie wyświetlanymi, usuwa z listy te, które już nie są wyświetlane
            dodaje na mapę znaczniki, których jeszcze nie ma
            Lista pobrana z serwera zawsze zawiera wszystkie znaczniki, które aktualnie powinny być wyświetlane
            porównuję je z tymi obecnie wyświetlanymi żeby nie tworzyć za każdym razem nie potrzebnie tych samych, które już są wyświetlane
         */
        @Override
        protected void onPostExecute(List<MarkerInfo> newMarkers){
            for(MarkerInfo marker:currentlyVisibleMarkers){
                if(newMarkers.contains(marker)){
                    newMarkers.remove(marker);
                }
                else{
                    currentlyVisibleMarkers.remove(marker);
                }
            }
            currentlyVisibleMarkers.addAll(newMarkers);
            for(MarkerInfo marker:newMarkers){
                Marker m =map.addMarker(new MarkerOptions()
                        .position(marker.getPosition())
                        .title(marker.getTrackName()));
                m.setTag(marker.getTrackId());
            }
        }
    }
}
