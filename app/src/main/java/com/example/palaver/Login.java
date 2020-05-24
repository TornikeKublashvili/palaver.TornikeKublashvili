package com.example.palaver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Login extends AppCompatActivity {
    private String nikName = "";
    private String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        ConstraintLayout constraintLayout = findViewById(R.id.ConstraintLayout);
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                assert inputMethodManager != null;
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });


        final EditText editTextNikName = findViewById(R.id.EditText_NikName);
        editTextNikName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                nikName = String.valueOf(editTextNikName.getText());
            }
        });

        final EditText editTextPassword = findViewById(R.id.EditText_Password);
        editTextPassword.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                password = String.valueOf(editTextPassword.getText());
            }
        });

        Button buttonLogIn = findViewById(R.id.Button_LogIn);
        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(nikName.length() < 1){
                Info.show(Login.this, getString(R.string.nikName_is_empty), Info.Color.Red);
            }
            else if(password.length() < 1){
                Info.show(Login.this, getString(R.string.password_is_empty), Info.Color.Red);
            }
            else{
                if (Info.isNetworkAvailable(Login.this)) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("Username", nikName);
                        json.put("Password", password);

                        JSONObject response = new NetworkHelper().execute("api/user/validate", json.toString()).get();
                        if(response.getInt("MsgType") == 0){
                            Info.show(Login.this, response.getString("Info"), Info.Color.Red);
                        }
                        else{
                            MainActivity.DB.insertUser(nikName, password);
                            MainActivity.DB.setLoggedIn(nikName, password, 1);

                            loadChatHistoryForAllUser(nikName, password);
                            Intent intent = new Intent(Login.this, ContactList.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        Info.show(Login.this, e.getMessage(), Info.Color.Red);
                        Log.d("LOG_Login", e.toString());
                    }
                }
                else if(MainActivity.DB.isValideUser(nikName, password)){
                    MainActivity.DB.setLoggedIn(nikName, password, 1);
                    Intent intent = new Intent(Login.this, ContactList.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Info.show(Login.this, getString(R.string.error_while_login), Info.Color.Red);
                    Log.d("LOG_Login", "no internet connection");
                }
            }
            }
        });

        TextView textViewRegister = findViewById(R.id.TextView_Register);
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });
    }

    private void loadChatHistoryForAllUser(String nikName, String password){
        refreschContactList();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String date =  dateFormat.format(cal.getTime());

        ArrayList<String > friends = MainActivity.DB.getFriends(nikName);
        for(String friend:friends){
            insertChatHistoriIntoDB(nikName, password, friend, date);
        }
    }
    private void insertChatHistoriIntoDB(String nikName,String password,String chatPartner, String fromDate) {
        try {
            JSONObject json = new JSONObject();
            json.put("Username", nikName);
            json.put("Password", password);
            json.put("Recipient", chatPartner);
            json.put("Offset", fromDate);
            JSONObject response = new NetworkHelper().execute("api/message/getoffset", json.toString()).get();

            JSONArray jarray = response.getJSONArray("Data");
            JSONObject jitem;
            for (int i = 0; i < jarray.length(); i++) {
                jitem = jarray.getJSONObject(i);
                String sender = jitem.getString("Sender");
                String recipient = jitem.getString("Recipient");
                String mimetype = jitem.getString("Mimetype");
                String data = jitem.getString("Data");
                String datetime = jitem.getString("DateTime");
                MainActivity.DB.insertMessage(datetime, nikName+chatPartner, sender, recipient, mimetype, data);
            }
        } catch (Exception e) {
            Log.d("LOG_Login", e.toString());
        }
    }

    private void refreschContactList(){
        try{
            JSONObject json = new JSONObject();
            json.put("Username", nikName);
            json.put("Password",  password);

            JSONObject response = new NetworkHelper().execute("api/friends/get", json.toString()).get();

            if(response.getInt("MsgType")==1){
                JSONArray jarray = response.getJSONArray("Data");
                for (int i = 0; i < jarray.length(); i++) {
                    MainActivity.DB.insertFriend(nikName, jarray.getString(i));
                }
            }
        }
        catch(Exception e){
            Log.d("LOG_Login", e.toString());
        }
    }
}
