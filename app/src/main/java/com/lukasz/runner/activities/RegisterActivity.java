package com.lukasz.runner.activities;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.lukasz.runner.R;
import com.lukasz.runner.Utilities;
import com.lukasz.runner.com.lukasz.runner.dialogs.InfoDialog;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Lukasz on 2017-09-11.
 */

public class RegisterActivity extends Activity implements View.OnTouchListener, View.OnClickListener{

    private EditText loginInput, passwordInput, repeatPasswordInput;
    private Dialog userCreatedDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regitration_layout);
        loginInput=(EditText) findViewById(R.id.registerLoginInput);
        loginInput.setOnTouchListener(this);
        passwordInput=(EditText) findViewById(R.id.registerPasswordInput);
        passwordInput.setOnTouchListener(this);
        repeatPasswordInput=(EditText) findViewById(R.id.registerRepeatPasswordInput);
        repeatPasswordInput.setOnTouchListener(this);
    }



    /*
        Po zapytaniu uprawnień i dostępu do neta sprawdza czy żadne pole nie jest puste.
        Później sprawdza czy haśło jest 2x takie samo. Jeśli tak to wysyła dane do stworzenia konta.
        Jeśli webservice zwróci treu użytkownik został stworzony, jeśli false naszwa jest zajęta.
     */
    public void register(View view){
        try{
            Utilities.requestInternetPermissions(this);
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if(connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null) {
                if (isAnyFieldEmpty()) {
                    return;
                }
                String login = loginInput.getText().toString();
                String password = passwordInput.getText().toString();
                if (!password.equals(repeatPasswordInput.getText().toString())) {
                    repeatPasswordInput.setText("");
                    repeatPasswordInput.setHintTextColor(Color.RED);
                    repeatPasswordInput.setHint("Hasło nie jest takie samo");
                    return;
                }
                CreateUser asyncTask = new CreateUser();
                asyncTask.execute(login, password);
                Boolean isCreated = asyncTask.get(5, TimeUnit.SECONDS);
                if(isCreated){
                   userCreatedDialog = InfoDialog.showOkDialog(this, "Pomyślnie utworzono użytkownika", this);
                }
                else{
                    InfoDialog.showOkDialog(this, "Ta nazwa użytkownika jest już zajęta!");
                }
            }
            else{
                InfoDialog.showOkDialog(this, "Brak połączenia z internetem.");   //nie wykryto sieci
            }
        }
        catch(InterruptedException  | ExecutionException e){
            InfoDialog.showOkDialog(this, "błąd w metodzie obsługującej przycisk");
        }
        catch(TimeoutException e) {
            InfoDialog.showOkDialog(this, "Brak odpowiedzi serwera.");   //serwer nie odpowiedział przez 5 sekunf
            e.printStackTrace();
        }
    }



    //jeśli któreś z pól jest puste ustawia w nim czerwoną informację o tym niefortunnym zdarzeniu
    private boolean isAnyFieldEmpty(){
        boolean result=false;
        String a = loginInput.getText().toString();
        if(loginInput.getText().toString().equals("")){
            loginInput.setHintTextColor(Color.RED);
            loginInput.setHint("Wypełnij to pole");
            result=true;
        }
        if(passwordInput.getText().toString().equals("")){
            passwordInput.setHintTextColor(Color.RED);
            passwordInput.setHint("Wypełnij to pole");
            result=true;
        }
        if(repeatPasswordInput.getText().toString().equals("")){
            repeatPasswordInput.setHintTextColor(Color.RED);
            repeatPasswordInput.setHint("Wypełnij to pole");
            result=true;
        }
        return result;
    }



    /*
        podmienia czerwony tekst o pustym polu z powrotem na pomarańczową nazwę pola
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.equals(loginInput)){
            loginInput.setHint("Nazwa użytkownika");
            loginInput.setHintTextColor(Color.parseColor("#ffb472"));
        }
        else if(v.equals(passwordInput)){
            passwordInput.setHint("Hasło");
            passwordInput.setHintTextColor(Color.parseColor("#ffb472"));
        }
        else if(v.equals(repeatPasswordInput)){
            repeatPasswordInput.setHint("Powtórz hasło");
            repeatPasswordInput.setHintTextColor(Color.parseColor("#ffb472"));
        }
        return false;
    }

    //obsługa przycisku "ok" w dialogu po utworzeniu użytkownika
    @Override
    public void onClick(View v) {
        userCreatedDialog.dismiss();
        finish();
    }


    private class CreateUser extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String url = getString(R.string.server)+getString(R.string.ws_register)+"?name="+params[0]+"&password="+params[1];
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            Boolean isCreated = restTemplate.getForObject(url, Boolean.class);
            return isCreated;
        }
    }
}
