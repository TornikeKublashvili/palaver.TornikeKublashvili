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

public class Register extends AppCompatActivity {
    private String nikName ="";
    private String password ="";
    private String passwordConfirm ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        ConstraintLayout constaintLayout = findViewById(R.id.ConstraintLayout);
        constaintLayout.setOnClickListener(new View.OnClickListener() {
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

        final EditText editTextPasswordConfirm = findViewById(R.id.EditText_Password_Confirm);
        editTextPasswordConfirm.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                passwordConfirm = String.valueOf(editTextPasswordConfirm.getText());
            }
        });

        Button buttonRegister = findViewById(R.id.Button_Register);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nikName.length() < 1){
                    Info.show(Register.this, getString(R.string.nikName_is_empty), Info.Color.Red);
                }
                else if(password.length() < 1){
                    Info.show(Register.this, getString(R.string.password_is_empty), Info.Color.Red);
                }
                else if(password.length() < 6){
                    Info.show(Register.this, getString(R.string.password_not_long_enough), Info.Color.Red);
                }
                else if (!password.equals(passwordConfirm)) {
                    Info.show(Register.this, getString(R.string.please_confirm_password), Info.Color.Red);
                }
                else{
                    try{
                        JSONObject json = new JSONObject();
                        json.put("Username", nikName);
                        json.put("Password", password);

                        JSONObject response = new NetworkHelper().execute("api/user/register", json.toString()).get();

                        if(response.getInt("MsgType") == 0){
                            Info.show(Register.this, response.getString("Info"), Info.Color.Red);
                        }
                        else{
                            Info.show(Register.this, response.getString("Info") + " " + getString(R.string.please_log_in), Info.Color.Green);
                            Intent intent = new Intent(Register.this, Login.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    catch(Exception e){
                        Log.d("LOG_Register", e.toString());
                    }
                }
            }
        });

        TextView textViewLogIn = findViewById(R.id.TextView_LogIn);
        textViewLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            }
        });
    }
}
