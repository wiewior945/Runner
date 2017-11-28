package com.lukasz.runner.activities;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lukasz.runner.R;
import com.lukasz.runner.Utilities;
import com.lukasz.runner.com.lukasz.runner.dialogs.InfoDialog;
import com.lukasz.runner.entities.Track;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Lukasz on 2017-10-04.
 */

public class SaveTrackActivity extends Activity implements View.OnTouchListener, View.OnClickListener{

    private Track track;
    private TextView timeTextView;
    private EditText nameEditText, startDescriptionEditText, finishDescriptionEditText;
    private Dialog trackSavedDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_track_activity);
        track = getIntent().getExtras().getParcelable("track");
        timeTextView = (TextView)findViewById(R.id.saveTrackTimeTextView);
        startDescriptionEditText = (EditText)findViewById(R.id.saveTrackStartDescriptionEditText);
        finishDescriptionEditText = (EditText)findViewById(R.id.saveTrackFinishDescriptionEditText);
        nameEditText = (EditText)findViewById(R.id.saveTrackNameEditText);
        nameEditText.setOnTouchListener(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        timeTextView.setText("Twój czas: "+track.getTime());
    }

    public void cancelTrackSave(View view){
        finish();
    }

    public void saveTrack(View view){
        String trackName = nameEditText.getText().toString();
        if(trackName.equals("")){
            nameEditText.setHintTextColor(Color.RED);
            nameEditText.setHint(getString(R.string.empty_field_warning));
            return;
        }
        track.setName(trackName);
        track.setStartDescription(startDescriptionEditText.getText().toString());
        track.setFinishDescription(finishDescriptionEditText.getText().toString());
        try{
            Utilities.requestInternetPermissions(this);
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if(connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null) {
                SaveTrack asyncTask = new SaveTrack();
                asyncTask.execute(track);
                Boolean isCreated = asyncTask.get(5, TimeUnit.SECONDS);
                if(isCreated){
                    trackSavedDialog = InfoDialog.showOkDialog(this, "Pomyślnie zapisano trasę", this);
                }
                else{
                    InfoDialog.showOkDialog(this, "Wystąpił błąd podczas zapisywania, spróbuj ponownie");
                }
            }
            else{
                InfoDialog.showOkDialog(this, "Brak połączenia z internetem.");   //nie wykryto sieci
            }
        }
            catch(InterruptedException  | ExecutionException e){
                e.printStackTrace();
                //InfoDialog.showOkDialog(this, "błąd w metodzie obsługującej przycisk");
        }
            catch(TimeoutException e) {
            InfoDialog.showOkDialog(this, "Brak odpowiedzi serwera.");   //serwer nie odpowiedział przez 5 sekund
            e.printStackTrace();
        }
    }

    /*
        Gdy nazwa trasy jest pusta wyświetlany jest czerowny komunikat. Ta metoda ustawia standartową podpowiedź po kliknięciu pola z nazwą.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.equals(nameEditText)){
            nameEditText.setHint(getString(R.string.provide_name));
            nameEditText.setHintTextColor(ContextCompat.getColor(this, R.color.lightOrange));
        }
        return false;
    }

    //dla przycisku "ok" z dialogu po pomyslnym zapisaniu trasy
    @Override
    public void onClick(View v) {
        trackSavedDialog.dismiss();
        finish();
    }


    private class SaveTrack extends AsyncTask<Track, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Track... params) {
            String url = getString(R.string.server)+getString(R.string.ws_save_track);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            Boolean isSaved = restTemplate.postForObject(url, params[0], Boolean.class);
            return isSaved;
        }
    }
}
