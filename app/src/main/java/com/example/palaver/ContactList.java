package com.example.palaver;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ContactList extends AppCompatActivity {
    private RemoveContact removeContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getString(R.string.contact_list));
    }

    @Override
    public void onResume(){
        super.onResume();

        if(Info.isNetworkAvailable(ContactList.this)){
            refreschContactList();
        }
        LinearLayout linearLayoutContactList = findViewById(R.id.LinearLayout_ContactList);
        addFriends(linearLayoutContactList);
    }

    private void refreschContactList(){
        try{
            JSONObject json = new JSONObject();
            json.put("Username", MainActivity.sharedPreferences.getString("NikName", ""));
            json.put("Password",  MainActivity.sharedPreferences.getString("Password", ""));

            JSONObject response = new NetworkHelper().execute("api/friends/get", json.toString()).get();

            if(response.getInt("MsgType")==0){
                Info.show(ContactList.this, response.getString("Info"), Info.Color.Red);
            }
            else if(response.getInt("MsgType")==1){
                JSONArray jarray = response.getJSONArray("Data");
                HashSet<String> contacts = new HashSet<>();
                for (int i = 0; i < jarray.length(); i++) {
                    contacts.add(jarray.getString(i));
                }
                MainActivity.sharedPreferences.edit().putStringSet("ContactList", contacts).apply();
            }
        }
        catch(Exception e){
            Log.d("LOG_AddOrRemoveContact", e.toString());
        }
    }

    public void addFriends(LinearLayout linearLayout){
        Set<String> contactListSet = MainActivity.sharedPreferences.getStringSet("ContactList", new HashSet<String>());
        ArrayList<String> contactListList = new ArrayList(contactListSet);
        Collections.sort(contactListList);

        if(linearLayout.getChildCount() > 0){
            linearLayout.removeAllViews();
        }
        LinearLayout.LayoutParams lp_tv = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if(contactListList.size() > 0){
            for(String friend: contactListList){
                final TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextView_Frends_List));
                textView.setText(friend);
                textView.setLayoutParams(lp_tv);
                linearLayout.addView(textView);

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.sharedPreferences.edit().putString("ChatPartner", textView.getText().toString()).apply();
                        Intent intent = new Intent(ContactList.this, ActivityChat.class);
                        startActivity(intent);
                    }
                });

                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        removeContact = new RemoveContact();
                        Bundle args = new Bundle();
                        args.putString("contact", textView.getText().toString());
                        removeContact.setArguments(args);
                        removeContact.show(getFragmentManager(), textView.getText().toString());

/*
                    removeFriend(textView.getText().toString());
*/
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.options_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.Log_Out){
            MainActivity.sharedPreferences.edit().putBoolean("IsLoggedIn", false).apply();
            Intent intent = new Intent(ContactList.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.Change_Password){
            Intent intent = new Intent(ContactList.this, ChangePassword.class);
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.Button_Add_Contact){
            AddContact addContact = new AddContact();
            addContact.show(getFragmentManager(), "");
        }
        else if(item.getItemId()== R.id.App_Version){
            Intent intent = new Intent(ContactList.this, Version.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
