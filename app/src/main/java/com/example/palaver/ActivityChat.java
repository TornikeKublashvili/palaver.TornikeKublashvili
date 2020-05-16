package com.example.palaver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityChat extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText editTextChatMessage;
    private String nikName;
    private String password;
    private String chatPartner;
    private ListView listViewChat;
    private LinearLayout linearLayoutAttachment;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private List<ChatMessage> chatMessagesToSend = new ArrayList<>();

    private ChatAdapter chatAdapter;

    FusedLocationProviderClient mFusedLocationClient;

    GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        nikName = MainActivity.sharedPreferences.getString("NikName", "");
        password = MainActivity.sharedPreferences.getString("Password", "");
        chatPartner = MainActivity.sharedPreferences.getString("ChatPartner", "");

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(chatPartner);

        listViewChat = findViewById(R.id.ListViev_Chat);
        listViewChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ChatMessage message = (ChatMessage) adapterView.getItemAtPosition(i);
            if (message.getMimetype().equals("location/plain")) {
                String[] params = message.getText().split(":");
                Uri uri = Uri.parse("geo:0,0?q=" +Float.parseFloat(params[1]) +"," + Float.parseFloat(params[2]) +"(Google)");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
            }
        });

        LinearLayout linearLayoutChat = findViewById(R.id.LinearLayout_Chat);
        linearLayoutChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                assert inputMethodManager != null;
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        linearLayoutAttachment = findViewById(R.id.LinearLayout_Attachment);

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
        ImageButton imageButtonChatSend = findViewById(R.id.ImageButton_Chat_Send);
        imageButtonChatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        ImageButton imageButtonChatAttach = findViewById(R.id.ImageButton_Chat_Attach);
        imageButtonChatAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(linearLayoutAttachment.getVisibility() == View.VISIBLE){
                linearLayoutAttachment.setVisibility(View.GONE);
            }
            else{
                linearLayoutAttachment.setVisibility(View.VISIBLE);
            }
            }
        });

        ImageButton imageButtonChooseLocation = findViewById(R.id.ImageButton_Choose_Location);
        imageButtonChooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        ImageButton imageButtonCHooseImage = findViewById(R.id.ImageButton_Choose_Image);
        imageButtonCHooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        ImageButton imageButtonChooseVideo = findViewById(R.id.ImageButton_Choose_Video);
        imageButtonChooseVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        ImageButton imageButtonChooseDoc = findViewById(R.id.ImageButton_Choose_Doc);
        imageButtonChooseDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        chatAdapter = new ChatAdapter(this, chatMessages);
        listViewChat.setAdapter(chatAdapter);
        getChatHistory();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected  void onResume(){
        super.onResume();
        loadAndSaveChatHistory();
    }

    private void sendMessage(){
        if(editTextChatMessage.getText().toString().length() > 0){
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
        sendAttachements();
    }

    private void sendAttachements(){
        if(chatMessagesToSend.size() > 0) {
            if (Info.isNetworkAvailable(ActivityChat.this)) {
                for(ChatMessage chatMessage: chatMessagesToSend) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("Username", nikName);
                        json.put("Password", password);
                        json.put("Recipient", chatPartner);
                        json.put("MimeType", chatMessage.getMimetype());
                        json.put("Data", chatMessage.getText());

                        JSONObject response = new NetworkHelper().execute("api/message/send", json.toString()).get();

                        if (response.getInt("MsgType") == 0) {
                            Info.show(ActivityChat.this, response.getString("Info"), Info.Color.Red);
                            Log.d("LOG_ActivityChat", response.toString());
                        } else {
                            loadAndSaveChatHistory();
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                            JSONObject data = (JSONObject)response.get("Data");
                            chatMessages.add(new ChatMessage(nikName, chatPartner, dateFormat.parse(data.getString("DateTime")), chatMessage.getMimetype(), chatMessage.getText()));
                            chatAdapter.notifyDataSetChanged();
                            listViewChat.setSelection(chatAdapter.getCount()-1);
                            editTextChatMessage.setText("");                        }
                    } catch (Exception e) {
                        Info.show(ActivityChat.this, e.getMessage(), Info.Color.Red);
                        Log.d("LOG_ActivityChat", e.toString());
                    }
                }
                chatMessagesToSend.clear();
            }
            else {
                Info.show(ActivityChat.this, getString(R.string.no_internet_connection), Info.Color.Red);
                Log.d("LOG_ActivityChat", "no internet connection");
             }
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

    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        chatMessagesToSend.add(new ChatMessage(nikName, chatPartner, "location/plain", location.getAltitude() + ":" + location.getLatitude() + ":" + location.getLongitude() + ":"));
        //TODO set loction button klickable false
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
