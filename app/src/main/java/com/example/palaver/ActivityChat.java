package com.example.palaver;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;


import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityChat extends AppCompatActivity {

    private EditText editTextChatMessage;
    private String nikName;
    private String password;
    private String chatPartner;
    private ListView listViewChat;
    private LinearLayout linearLayoutChat;
    private ImageButton imageButtonChatSend;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        nikName = MainActivity.sharedPreferences.getString("NikName", "");
        password = MainActivity.sharedPreferences.getString("Password", "");
        chatPartner = MainActivity.sharedPreferences.getString("ChatPartner", "");

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(chatPartner);

        listViewChat = findViewById(R.id.ListViev_Chat);

        linearLayoutChat = findViewById(R.id.LinearLayout_Chat);
        linearLayoutChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        editTextChatMessage = findViewById(R.id.EditText_Chat_Message);
        editTextChatMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        imageButtonChatSend =  findViewById(R.id.ImageButton_Chat_Send);
        imageButtonChatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        chatAdapter = new ChatAdapter(this, chatMessages);
        listViewChat.setAdapter(chatAdapter);
        getChatHistory();
    }

    @Override
    protected  void onResume(){
        super.onResume();
        loadAndSaveChatHistory();
    }

    private void sendMessage(){
        if(Info.isNetworkAvailable(ActivityChat.this)){
            try {
                JSONObject json = new JSONObject();
                json.put("Username", nikName);
                json.put("Password", password);
                json.put("Recipient", chatPartner);
                json.put("MimeType", "text/plain");
                json.put("Data", editTextChatMessage.getText().toString());

                JSONObject response = new NetworkHelper().execute("api/message/send", json.toString()).get();

                if(response.getInt("MsgType") == 0){
                    Info.show(ActivityChat.this, response.getString("Info"), Info.Color.Red);
                    Log.d("LOG_ActivityChat", response.toString());
                }
                else{
                    loadAndSaveChatHistory();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    JSONObject data = (JSONObject)response.get("Data");
                    chatMessages.add(new ChatMessage(nikName, chatPartner, dateFormat.parse(data.getString("DateTime")), "text/plain", editTextChatMessage.getText().toString()));
                    chatAdapter.notifyDataSetChanged();
                    listViewChat.setSelection(chatAdapter.getCount()-1);
                    editTextChatMessage.setText("");
                }
            } catch (Exception e) {
                Info.show(ActivityChat.this, e.getMessage(), Info.Color.Red);
                Log.d("LOG_ActivityChat", e.toString());
            }
        }
        else{
            Info.show(ActivityChat.this, getString(R.string.no_internet_connection), Info.Color.Red);
            Log.d("LOG_ActivityChat", "no internet connection");
        }
    }
    private void getChatHistory() {
        try {
            JSONObject json = new JSONObject();
            json.put("Username", nikName);
            json.put("Password", password);
            json.put("Recipient", chatPartner);
            JSONObject response;
            if(Info.isNetworkAvailable(ActivityChat.this)) {
                response = new NetworkHelper().execute("api/message/get", json.toString()).get();
            }
            else {
                response = new JSONObject(MainActivity.sharedPreferences.getString("Chat"+nikName+chatPartner, ""));
            }
            if (response.getInt("MsgType") == 0) {
                Info.show(ActivityChat.this, response.getString("Info"), Info.Color.Red);
                Log.d("LOG_ActivityChat", response.toString());
            } else {
                chatMessages.clear();
                JSONArray jarray = response.getJSONArray("Data");
                JSONObject jitem;
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

                for (int i = 0; i < jarray.length(); i++) {
                    jitem = jarray.getJSONObject(i);
                    String sender = jitem.getString("Sender");
                    String recipient = jitem.getString("Recipient");
                    String mimetype = jitem.getString("Mimetype");
                    String data = jitem.getString("Data");
                    Date date = dateFormat.parse(jitem.getString("DateTime"));

                    chatMessages.add(new ChatMessage(sender, recipient, date, mimetype, data));
                    chatAdapter.notifyDataSetChanged();
                    listViewChat.setSelection(chatAdapter.getCount() - 1);
                }
            }
        } catch (Exception e) {
            Log.d("LOG_ActivityChat", e.toString());
        }
    }

    private void loadAndSaveChatHistory() {
        try {
            JSONObject json = new JSONObject();
            json.put("Username", nikName);
            json.put("Password", password);
            json.put("Recipient", chatPartner);
            JSONObject response  = new NetworkHelper().execute("api/message/get", json.toString()).get();

            if (response.getInt("MsgType") == 0) {
                Log.d("LOG_ActivityChat", response.toString());
            } else {
                MainActivity.sharedPreferences.edit().putString("Chat"+nikName+chatPartner, response.toString()).apply();
                Log.d("LOG_ActivityChat", "Save Chat Histori from " + nikName + " to" + chatPartner);
            }
        } catch (Exception e) {
            Log.d("LOG_ActivityChat", e.toString());
        }
    }
}
