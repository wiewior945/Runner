package com.lukasz.runner.activities;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lukasz.runner.MarkerInfoWindow;
import com.lukasz.runner.R;
import com.lukasz.runner.com.lukasz.runner.dialogs.InfoDialog;
import com.lukasz.runner.entities.Track;
import com.lukasz.runner.entities.TrackTime;
import com.lukasz.runner.entities.User;
import com.lukasz.runner.services.GpsService;
import com.lukasz.runner.services.GpsService.CreateBinder;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowCloseListener, View.OnClickListener{

    private GoogleMap map;
    private DrawerLayout drawerMenu;
    private LinearLayout menu;
    private Button newTrackButton, cancelTrackButton, endTruckTextButton;
    private ImageView endTrackButton, centerMapButton, navigateButton, trackTimesButton,showFinishForTackButton, startTrackButton, cancelRunButton;
    private TextView timerTextView;
    private GpsService gpsService;
    private User user;
    private Handler handler = new Handler();
    private Runnable trackTimer;
    private List<Marker> currentlyVisibleMarkers = new ArrayList<>();
    private Marker selectedMarker;
    private Marker finishMarker;
    private boolean trackingFlag=true;  //czy mapa ma podążać za znacznikiem (obecnym  miejscem)
    private boolean gpsFlag=false;      //czy LocationListener jest uruchomiony
    private boolean isFinishShowed=false;   //czy pokazywane jest marker z metą trasy
    private boolean downloadTracks=true;    //czy trasy mają być pobierane z serwera i wrzucane na mapę jako markery
    private boolean isTrackRunning=false;


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
        navigateButton=(ImageView) findViewById(R.id.navigateButton);
        trackTimesButton=(ImageView) findViewById(R.id.trackTimesButton);
        showFinishForTackButton=(ImageView) findViewById(R.id.showFinishForTrackButton);
        startTrackButton=(ImageView) findViewById(R.id.startTrackButton);
        cancelRunButton=(ImageView) findViewById(R.id.cancelRunButton);

        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        Intent intent = new Intent(this, GpsService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        Bundle data = getIntent().getExtras();
        user = data.getParcelable("user");
    }


    @Override
    public void onResume(){
        super.onResume();
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


    //================================== INTERFACES ===================================
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng warsaw = new LatLng(52.23, 21.006);
        map = googleMap;
        map.setOnCameraIdleListener(this);
        map.setMyLocationEnabled(true);
        map.setInfoWindowAdapter(new MarkerInfoWindow(this));
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowCloseListener(this);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false); //ukrywa domyślny przycisk googla do namierzania obecnej lokalizacji
        map.getUiSettings().setMapToolbarEnabled(false);    //po naciśnięciu Marker'a pojawiają się googlowe przyciski, to je wyłącza
        map.animateCamera(CameraUpdateFactory.newLatLng(warsaw));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    //liestener obsługujący poruszanie się mapy, ładuje trasy gdy mapa zostanie przesunięta
    // gdy tworzona jest nowa trasa nie wyświetla markerów
    @Override
    public void onCameraIdle() {
        if(!gpsService.getRecordTrack() && downloadTracks){
            LatLng rightBottom = map.getProjection().getVisibleRegion().nearRight;
            LatLng leftTop = map.getProjection().getVisibleRegion().farLeft;
            new UpdateTracksOnMap().execute(leftTop,rightBottom);
        }
    }

    // pokazuje przyciski po nacisnieciu markera
    // markery mapy nie mają Track zapisanej w Tag markera
    // jeśli kliknięty zostanie marker mety nic sie nie dzieje
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTag() == null){
            return true;
        }
        hideFinishMarker();
        selectedMarker = marker;
        navigateButton.setVisibility(View.VISIBLE);
        trackTimesButton.setVisibility(View.VISIBLE);
        showFinishForTackButton.setVisibility(View.VISIBLE);
        return false;
    }

    //ukrywa przyciski pojawiajace sie po nacisnieciu markera
    @Override
    public void onInfoWindowClose(Marker marker) {
        //hideFinishMarker();
        selectedMarker = null;
        navigateButton.setVisibility(View.GONE);
        trackTimesButton.setVisibility(View.GONE);
        showFinishForTackButton.setVisibility(View.GONE);
        startTrackButton.setVisibility(View.GONE);
    }

    // obsługuje TextView z dialogu z czasami trasy
    @Override
    public void onClick(View view){
        TextView textView = (TextView) view;
        System.out.println(textView.getHint());
    }
    //==============================================================================================================



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
        TrackTime trackTime = gpsService.saveTrack();
        if(trackTime!=null){                        // jeśli jest nullem w GPS service wyświetlany jest komunikat o braku ruchu
            trackTime.setTime(timerTextView.getText().toString());
            Intent intent = new Intent(this, SaveTrackActivity.class);
            intent.putExtra("trackTime", trackTime);
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
                    downloadTracks = true;
                    isTrackRunning = false;
                    showHiddenMarkers();
                    selectedMarker = null;
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

    /*
        pokazuje marker mety i wyśrodkowuje mapę, zatrzymuje wyśrodkowywanie mapy na pozycji użytkownika
     */
    public void showFinish(View view){
        if(!isFinishShowed) {
            isFinishShowed = true;
            downloadTracks = false;
            if(trackingFlag)trackingButton(null);
            hideMarkers(selectedMarker);
            Track track = (Track) selectedMarker.getTag();
            finishMarker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(track.getEndLatitude(), track.getEndLongitude()))
                    .title(track.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            showFinishForTackButton.setBackground(ContextCompat.getDrawable(this, R.drawable.finish_icon));
            Double centerLat = null, centerLng = null;
            if(track.getStartLatitude()>track.getEndLatitude()){
                centerLat = track.getEndLatitude() + ((track.getStartLatitude() - track.getEndLatitude())/2);
            }else{
                centerLat = track.getStartLatitude() + ((track.getEndLatitude() - track.getStartLatitude())/2);
            }
            if(track.getStartLongitude()>track.getEndLongitude()){
                centerLng = track.getEndLongitude() + ((track.getStartLongitude() - track.getEndLongitude())/2);
            }else{
                centerLng = track.getStartLongitude() + ((track.getEndLongitude() - track.getStartLongitude())/2);
            }
            CameraUpdate updatedLocation = CameraUpdateFactory.newLatLng(new LatLng(centerLat, centerLng));
            map.animateCamera(updatedLocation);
        }
        else{
            if(!trackingFlag) trackingButton(null);
            hideFinishMarker();
            downloadTracks=true;
        }


    }

    public void navigateButton(View view){
        Track track = (Track) selectedMarker.getTag();
        Uri destination = Uri.parse("google.navigation:q="+track.getStartLatitude().toString()+","+track.getStartLongitude().toString()+"&mode=w");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, destination);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void trackTimesButton(View view){
//        Map<Long, String> map = new LinkedHashMap<>();
//        map.put(4L, "aaa");
//        map.put(12L, "bbb");
//        map.put(2L, "ccc");
//        map.put(16L, "ddd");
        try{
            Track track = (Track) selectedMarker.getTag();
            GetTrackTimes asyncTask = new GetTrackTimes();
            asyncTask.execute(track.getId());
            LinkedHashMap<String, String> map = asyncTask.get(5, TimeUnit.SECONDS);
            InfoDialog.showTrackTimesDialog(this, map);
        }catch(ExecutionException | InterruptedException ex){
            ex.printStackTrace();
        }catch(TimeoutException e){
            e.printStackTrace();
        }
    }

    public void startTrack(View view){
        startTrackButton.setVisibility(View.GONE);
        navigateButton.setVisibility(View.GONE);
        trackTimesButton.setVisibility(View.GONE);
        showFinishForTackButton.setVisibility(View.GONE);
        cancelRunButton.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.VISIBLE);
        showFinish(null);
        downloadTracks = false;
        isTrackRunning = true;
        hideMarkers(null);
        handler.post(new CountDown());
        gpsService.newTrack(user);
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

    // ukrywa wszystkie widoczne markery zostawiając tylko ten podany w parametrze, podając nulla ukrywa wszystkie
    private void hideMarkers(Marker marker){
        Long markerId = 0L;
        if(marker!=null){
            markerId = ((Track)marker.getTag()).getId();
        }
        for(Marker m : currentlyVisibleMarkers){
            if(!markerId.equals(((Track)m.getTag()).getId())){
                m.setVisible(false);
            }
        }
    }

    private void showHiddenMarkers(){
        for(Marker m : currentlyVisibleMarkers){
            m.setVisible(true);
        }
    }


    //jeśli wyświetlany jest marker pokazujący metę metoda usuwa go
    private void hideFinishMarker(){
        if(isFinishShowed){
            isFinishShowed=false;
            finishMarker.remove();
            showFinishForTackButton.setBackground(ContextCompat.getDrawable(this, R.drawable.grey_finish_icon));
            showHiddenMarkers();
        }
    }


    private void saveTrackTime(){
        isTrackRunning = false;
        downloadTracks = true;
        handler.removeCallbacks(trackTimer);
        TrackTime trackTime = gpsService.saveTrack();
        if(trackTime!=null) {                        // jeśli jest nullem w GPS service wyświetlany jest komunikat o braku ruchu
            trackTime.setTime(timerTextView.getText().toString());
            trackTime.setTrack((Track)selectedMarker.getTag());
        }
        hideFinishMarker();
        showHiddenMarkers();
        cancelRunButton.setVisibility(View.GONE);
        timerTextView.setVisibility(View.GONE);
        timerTextView.setText("00:00");
        String message = "Twój czas to: "+trackTime.getTime() + "\n" + "Czy chcesz zapisać trasę?";
        InfoDialog.showCancelDialog(this, message,  new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==(R.id.cancelDialogCancelButton)){ //CANCEL
                    Dialog d = (Dialog) v.getTag();
                    d.dismiss();
                }
                else if(v.getId()==R.id.cancelDialogOkButton){  //OK
                    try{
                        SaveTrackTime asyncTask = new SaveTrackTime();
                        asyncTask.execute(trackTime);
                        Boolean isCreated = asyncTask.get(15, TimeUnit.SECONDS);
                    }catch(InterruptedException  | ExecutionException e){
                        e.printStackTrace();
                    }
                    catch(TimeoutException e) {
                        e.printStackTrace();
                    }

                    Dialog d = (Dialog) v.getTag();
                    d.dismiss();
                }
            }
        });
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
        if(selectedMarker!=null && !isTrackRunning){
            float[] forDistance = new float[10];
            Location.distanceBetween(latitude, longtitude, selectedMarker.getPosition().latitude, selectedMarker.getPosition().longitude, forDistance);
            if(forDistance[0]<3) startTrackButton.setVisibility(View.VISIBLE);
            else startTrackButton.setVisibility(View.GONE);
        }
        if(isTrackRunning){
            float[] forDistance = new float[10];
            Location.distanceBetween(latitude, longtitude, finishMarker.getPosition().latitude, finishMarker.getPosition().longitude, forDistance);
            System.out.println("@@@ ---------- "+forDistance[0]);
            if(forDistance[0]<3) {
                saveTrackTime();
            }
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
    private class UpdateTracksOnMap extends AsyncTask<LatLng, Void, List<Track>>{

        @Override
        protected List<Track> doInBackground(LatLng... corners) {
            String url = getString(R.string.server)+getString(R.string.ws_update_track_markers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            /*  serwer zwraca listę HashMap, nie znalazłem metody do mapowania takich danych do obiektów
             *  dlatego ta pętla leci po wszystkich danych z serwera i tworzy z nich ArrayListę obiektów
             */
            List<LinkedHashMap> json = (List<LinkedHashMap>) restTemplate.postForObject(url, corners, List.class);
            ArrayList<Track> markers = new ArrayList<>();
            for(LinkedHashMap<String, Object>data:json){
                Track track =new Track();
                track.setId(new Long((Integer)data.get("id")));
                track.setName((String)data.get("name"));
                track.setStartLatitude((Double)data.get("startLatitude"));
                track.setStartLongitude((Double)data.get("startLongitude"));
                track.setEndLatitude((Double)data.get("endLatitude"));
                track.setEndLongitude((Double)data.get("endLongitude"));
                track.setDateCreated(new Date((Long)data.get("dateCreated"))); //z serwera dostaje milisekundy
                track.setStartDescription((String)data.get("startDescription"));
                track.setFinishDescription((String)data.get("finishDescription"));
                track.setDistance((Integer) data.get("distance"));
                markers.add(track);
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
        protected void onPostExecute(List<Track> newMarkersList){
            Iterator<Marker> visibleMarkers = currentlyVisibleMarkers.iterator();
            Iterator<Track> newMarkers = newMarkersList.iterator();
            while(visibleMarkers.hasNext()){
                Track track = (Track)visibleMarkers.next().getTag();
                boolean isRemoved = false;
                while(newMarkers.hasNext()){
                    if(newMarkers.next().getId().equals(track.getId())){
                        newMarkers.remove();
                        isRemoved = true;
                        break;
                    }
                }
                if(!isRemoved){
                    visibleMarkers.remove();
                }
            }


            for(Track track:newMarkersList){
                Marker m =map.addMarker(new MarkerOptions()
                        .position(new LatLng(track.getStartLatitude(), track.getStartLongitude()))
                        .title(track.getName()));
                m.setTag(track);
                currentlyVisibleMarkers.add(m);
            }
        }
    }


    private class GetTrackTimes extends AsyncTask<Long, Void, LinkedHashMap<String, String>>{

        @Override
        protected LinkedHashMap<String, String> doInBackground(Long... longs) {
            String url = getString(R.string.server) + getString(R.string.ws_get_five_best_track_times)+"?trackId="+longs[0].toString();
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            LinkedHashMap<String, String> map = restTemplate.getForObject(url, LinkedHashMap.class);
            return map;
        }
    }

    private class SaveTrackTime extends AsyncTask<TrackTime, Void, Boolean>{

        @Override
        protected Boolean doInBackground(TrackTime... params) {
            String timeTrackUrl = getString(R.string.server)+getString(R.string.ws_save_track_time);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            Boolean isSaved = restTemplate.postForObject(timeTrackUrl, params[0], Boolean.class);
            return isSaved;
            //TODO: wymazać trasę na baie jeśli zapis czasu się nie powiedzie
        }
    }
}
