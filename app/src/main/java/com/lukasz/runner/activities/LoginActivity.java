package com.lukasz.runner.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;

import com.lukasz.runner.R;
import com.lukasz.runner.com.lukasz.runner.dialogs.InfoDialog;
import com.lukasz.runner.entities.User;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Lukasz on 2017-08-28.
 */

public class LoginActivity extends Activity {

    private EditText loginEditText, passwordEditText;
    private final String SERVER = "http://192.168.8.100:8080/"; //pamiętać o http i porcie
    private final String WEB_SERVICE = "Runner/webservice/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        loginEditText = (EditText)findViewById(R.id.loginInput);
        passwordEditText = (EditText)findViewById(R.id.passwordInput);
    }

    /*
        Metoda przypięta do przycisku logowania. Na początku sprawdza uprawnienia do internetu.
        Sprawdza czy telefon jest podłączony do sieci, później wysyła zapytanie do serwera z danymi użytkownika.
        Jeśli serwer zwróci użytkownika logowanie przebiegło pomyślnie, jeśli dane nie pasują serwer zwraca nulla.
        Timeout jest ustawiony na 5 sekund, jeśli po tym czsie serwer nie odpowie wyświetlany jest komunikat.
     */
    public void login(View view){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 123);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 124);
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if(connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null){
            try {
                String login = loginEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                CheckUser asyncTask = new CheckUser();
                asyncTask.execute(login, password);
                User user = asyncTask.get(5, TimeUnit.SECONDS);
                if(user!=null){
                    Intent intent = new Intent(this, MapsActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }
                else InfoDialog.showDialog(this, "Podano błędny login lub hasło. Spróbuj ponownie."); //serwer zwrócił pusty wynik = błędny login/hasło
            } catch (InterruptedException  | ExecutionException e) {
                InfoDialog.showDialog(this, "błąd w metodzie obsługującej przycisk");
                e.printStackTrace();
            } catch (TimeoutException e) {
                InfoDialog.showDialog(this, "Brak odpowiedzi serwera.");   //serwer nie odpowiedział przez 5 sekunf
                e.printStackTrace();
            }
        }
        else{
            InfoDialog.showDialog(this, "Brak połączenia z internetem.");   //nie wykryto sieci
        }

    }


    private class CheckUser extends AsyncTask<String, Void, User>{

        @Override
        protected User doInBackground(String... params) {
            String url = SERVER+WEB_SERVICE+"?name="+params[0]+"&password="+params[1];
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            User user = restTemplate.getForObject(url, User.class);
            return user;
        }
    }
}
