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

import java.util.HashSet;
import java.util.Set;

public class AddOrRemoveContact extends AppCompatActivity {
    private String friendsNikName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getString(R.string.add_remove_contact));

        ConstraintLayout constraintLayout = findViewById(R.id.ConstraintLayout);
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
            }
        });

        final EditText editTextFriend = findViewById(R.id.EditText_Friend);
        editTextFriend.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                friendsNikName = String.valueOf(editTextFriend.getText());
            }
        });

        Button buttonAddFriend = findViewById(R.id.Button_Add_Friend);
        buttonAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(friendsNikName.length() < 1){
                Info.show(AddOrRemoveContact.this, getString(R.string.nikName_is_empty), Info.Color.Red);
            }
            else{
                if (Info.isNetworkAvailable(AddOrRemoveContact.this)) {
                    try{
                        JSONObject json = new JSONObject();
                        json.put("Username", MainActivity.sharedPreferences.getString("NikName", ""));
                        json.put("Password",  MainActivity.sharedPreferences.getString("Password", ""));
                        json.put("Friend", friendsNikName);

                        JSONObject response = new NetworkHelper().execute("api/friends/add", json.toString()).get();

                        if(response.getInt("MsgType")==0){
                            Info.show(AddOrRemoveContact.this, response.getString("Info"), Info.Color.Red);
                        }
                        else if(response.getInt("MsgType")==1){
                            Set<String> contactList = new HashSet<>(MainActivity.sharedPreferences.getStringSet("ContactList", new HashSet<String>()));
                            contactList.add(friendsNikName);
                            MainActivity.sharedPreferences.edit().putStringSet("ContactList", contactList).apply();
                            editTextFriend.setText("");
                            Info.show(AddOrRemoveContact.this, response.getString("Info"), Info.Color.Green);
                        }
                    }
                    catch(Exception e){
                        Info.show(AddOrRemoveContact.this, e.getMessage(), Info.Color.Red);
                        Log.d("LOG_AddOrRemoveContact", e.toString());
                    }
                }else {
                    Info.show(AddOrRemoveContact.this, getString(R.string.no_internet_connection), Info.Color.Red);
                    Log.d("LOG_AddOrRemoveContact", getString(R.string.no_internet_connection));
                }
            }
            }
        });

        Button buttonRemoveFriend = findViewById(R.id.Button_Remove_Friend);
        buttonRemoveFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(friendsNikName.length() < 1){
                Info.show(AddOrRemoveContact.this, getString(R.string.nikName_is_empty), Info.Color.Red);
            }
            else {
                if (Info.isNetworkAvailable(AddOrRemoveContact.this)) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("Username", MainActivity.sharedPreferences.getString("NikName", ""));
                        json.put("Password", MainActivity.sharedPreferences.getString("Password", ""));
                        json.put("Friend", friendsNikName);

                        JSONObject response = new NetworkHelper().execute("api/friends/remove", json.toString()).get();

                        if (response.getInt("MsgType") == 0) {
                            Info.show(AddOrRemoveContact.this, response.getString("Info"), Info.Color.Red);
                        } else if (response.getInt("MsgType") == 1) {
                            Set<String> contactList = new HashSet<>(MainActivity.sharedPreferences.getStringSet("ContactList", new HashSet<String>()));
                            contactList.remove(friendsNikName);
                            MainActivity.sharedPreferences.edit().putStringSet("ContactList", contactList).apply();
                            editTextFriend.setText("");
                            Info.show(AddOrRemoveContact.this, response.getString("Info"), Info.Color.Green);
                        }
                    }
                    catch(Exception e){
                        Info.show(AddOrRemoveContact.this, e.getMessage(), Info.Color.Red);
                        Log.d("LOG_AddOrRemoveContact", e.toString());
                    }
                }else {
                    Info.show(AddOrRemoveContact.this, getString(R.string.no_internet_connection), Info.Color.Red);
                    Log.d("LOG_AddOrRemoveContact", getString(R.string.no_internet_connection));
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
        menu.findItem(R.id.Button_Add_Contact).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.Log_Out){
            MainActivity.sharedPreferences.edit().putBoolean("IsLoggedIn", false).apply();
            Intent intent = new Intent(AddOrRemoveContact.this, MainActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.Change_Password){
            Intent intent = new Intent(AddOrRemoveContact.this, ChangePassword.class);
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.Button_Back){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
