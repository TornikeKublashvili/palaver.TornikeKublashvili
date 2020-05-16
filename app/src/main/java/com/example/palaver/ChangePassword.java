package com.example.palaver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONObject;

public class ChangePassword extends AppCompatActivity {
    private String newPassword ="";
    private String newPasswordConfirm ="";
    private String oldPassword ="";
    private String nikName = MainActivity.sharedPreferences.getString("NikName", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getString(R.string.change_password));

        ConstraintLayout constraintLayout = findViewById(R.id.ConstraintLayout);
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
            }
        });

        final EditText editTextNewPassword = findViewById(R.id.EditText_New_Password);
        editTextNewPassword.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                newPassword = String.valueOf(editTextNewPassword.getText());
            }
        });

        final EditText editTextNewPasswordConfirm = findViewById(R.id.EditText_New_Password_Confirm);
        editTextNewPasswordConfirm.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                newPasswordConfirm = String.valueOf(editTextNewPasswordConfirm.getText());
            }
        });

        final EditText editTextOldPassword = findViewById(R.id.EditText_Old_Password);
        editTextOldPassword.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                oldPassword = String.valueOf(editTextOldPassword.getText());
            }
        });

        Button buttonChangePassword = findViewById(R.id.Button_Change_Password);
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(newPassword.length() < 1){
                    Info.show(ChangePassword.this, getString(R.string.password_is_empty), Info.Color.Red);
                }
                else if(newPassword.length() < 6){
                    Info.show(ChangePassword.this, getString(R.string.password_not_long_enough), Info.Color.Red);
                }
                else if (!newPassword.equals(newPasswordConfirm)) {
                    Info.show(ChangePassword.this, getString(R.string.please_confirm_password), Info.Color.Red);
                }
                else{
                    try{
                        JSONObject json = new JSONObject();
                        json.put("Username", nikName);
                        json.put("Password", oldPassword);
                        json.put("NewPassword", newPassword);

                        JSONObject response = new NetworkHelper().execute("api/user/password", json.toString()).get();

                        if(response.getInt("MsgType")==0){
                            Info.show(ChangePassword.this, response.getString("Info"), Info.Color.Red);
                        }
                        else if(response.getInt("MsgType")==1){
                            editTextNewPassword.setText("");
                            editTextNewPasswordConfirm.setText("");
                            editTextOldPassword.setText("");
                            Info.show(ChangePassword.this, response.getString("Info"), Info.Color.Green);
                        }
                    }
                    catch (Exception e){
                        Log.d("LOG_ChangePassword", e.toString());
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.options_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.Change_Password).setVisible(false);
        menu.findItem(R.id.Button_Add_Contact).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.Log_Out){
            MainActivity.sharedPreferences.edit().putBoolean("IsLoggedIn", false).apply();
            Intent intent = new Intent(ChangePassword.this, MainActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.Change_Password){
            Intent intent = new Intent(ChangePassword.this, ChangePassword.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
