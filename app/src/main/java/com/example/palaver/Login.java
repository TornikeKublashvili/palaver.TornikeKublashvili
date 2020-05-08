package com.example.palaver;

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

import org.json.JSONObject;

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
                                MainActivity.sharedPreferences.edit().putBoolean("IsLoggedIn", true).apply();
                                MainActivity.sharedPreferences.edit().putString("NikName", nikName).apply();
                                MainActivity.sharedPreferences.edit().putString("Password", password).apply();
                                Intent intent = new Intent(Login.this, ContactList.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (Exception e) {
                            Info.show(Login.this, e.getMessage(), Info.Color.Red);
                            Log.d("LOG_Login", e.toString());
                        }
                    }
                    else{
                        Info.show(Login.this, getString(R.string.no_internet_connection), Info.Color.Red);
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
}
