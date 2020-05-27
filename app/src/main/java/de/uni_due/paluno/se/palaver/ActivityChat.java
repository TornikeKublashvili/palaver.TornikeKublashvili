package de.uni_due.paluno.se.palaver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static de.uni_due.paluno.se.palaver.R.layout.*;

public class ActivityChat extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText editTextChatMessage;
    private String nikName;
    private String password;
    private String chatPartner;
    private ListView listViewChat;
    private LinearLayout linearLayoutAttachment;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private List<ChatMessage> chatMessagesToSend = new ArrayList<>();
    private Timer timer;
    private ChatAdapter chatAdapter;
    private LinearLayout layoutAttachments;

    FusedLocationProviderClient mFusedLocationClient;

    GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_chat);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        nikName = MainActivity.nikName;
        password = MainActivity.password;
        chatPartner = MainActivity.chatPartner;

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(chatPartner);

        listViewChat = findViewById(R.id.ListViev_Chat);
        listViewChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ChatMessage message = (ChatMessage) adapterView.getItemAtPosition(i);
                if (message.getMimetype().equals("location/plain")) {
                    String[] params = message.getData().split(":");
                    Uri uri = Uri.parse("geo:0,0?q=" + Float.parseFloat(params[1]) + "," + Float.parseFloat(params[2]) + "(Google)");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                } else if (message.getMimetype().equals("Image/*")) {
                    boolean hasPermission = (ContextCompat.checkSelfPermission(ActivityChat.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                    if (!hasPermission) {
                        ActivityCompat.requestPermissions(ActivityChat.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);
                    } else {
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Palaver/Palaver Fotos";
                        File storageDir = new File(path);
                        if (!storageDir.exists() && !storageDir.mkdirs()) {
                            Log.d("LOG_ActivityChat", "create directory failed -> Palaver/Palaver Fotos");
                        }
                        Bitmap b = Methods.base64ToBitmap(message.getData());
                        final File foto = new File(storageDir, message.getSender() + "_" + message.getDate() + ".JPEG");
                        FileOutputStream fOut;
                        try {
                            fOut = new FileOutputStream(foto);
                            assert b != null;
                            b.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                            fOut.flush();
                            fOut.close();
                            ImageView imageViewMessage = view.findViewById(R.id.ImageView_Message);
                            imageViewMessage.setAlpha(1.0F);
                            TextView textViewMessageDownload = view.findViewById(R.id.TextView_Message_Download);
                            textViewMessageDownload.setVisibility(View.GONE);

                        } catch (IOException e) {
                            Log.d("LOG_MainActivity", e.toString());
                        }
                    }
                }
            }
        });

        LinearLayout linearLayoutChat = findViewById(R.id.LinearLayout_Chat);
        linearLayoutChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
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
                sendAttachements();
                sendMessage();
                if( layoutAttachments.getChildCount() > 0){
                    layoutAttachments.removeAllViews();
                }
            }
        });

        ImageButton imageButtonChatAttach = findViewById(R.id.ImageButton_Chat_Attach);
        imageButtonChatAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linearLayoutAttachment.getVisibility() == View.VISIBLE) {
                    linearLayoutAttachment.setVisibility(View.GONE);
                } else {
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
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                createImageBrowsingRequest();
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

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        String date =  dateFormat.format(cal.getTime());

        ArrayList<ChatMessage> messages = MainActivity.DB.getMessages(MainActivity.nikName + MainActivity.chatPartner, date);
        chatMessages.clear();
        chatMessages.addAll(messages);
        listViewChat.setSelection(chatAdapter.getCount()-1);

        layoutAttachments = findViewById(R.id.LinearLayout_Attachments);

    }
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        timer.cancel();
        super.onStop();
    }

    @Override
    protected  void onResume(){
        super.onResume();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ActivityChat.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if(MainActivity.DB.getCountunreadMessages(nikName, chatPartner) >0){
                            String date = MainActivity.DB.getMinDateFromUnreadMessages(nikName, chatPartner).replace(' ','T');
                            insertChatHistoriIntoDB(date);
                        }
                    }
                });
            }
        }, 0, 1000);
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
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        JSONObject data = (JSONObject)response.get("Data");
                        String datetime = data.getString("DateTime").substring(0, data.getString("DateTime").indexOf('.')).replace('T',' ');
                        MainActivity.DB.insertMessage(data.getString("DateTime"), MainActivity.nikName+MainActivity.chatPartner, nikName, chatPartner, "text/plain", editTextChatMessage.getText().toString());
                        chatMessages.add(new ChatMessage(nikName, chatPartner, datetime, "text/plain", editTextChatMessage.getText().toString(),0));
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
                Info.show(ActivityChat.this, getString(R.string.noInternetConnection), Info.Color.Red);
                Log.d("LOG_ActivityChat", "no internet connection");
            }
        }
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
                        json.put("Data", chatMessage.getData());

                        JSONObject response = new NetworkHelper().execute("api/message/send", json.toString()).get();

                        if (response.getInt("MsgType") == 0) {
                            Info.show(ActivityChat.this, response.getString("Info"), Info.Color.Red);
                            Log.d("LOG_ActivityChat", response.toString());
                        } else {
                            JSONObject data = (JSONObject)response.get("Data");
                            MainActivity.DB.insertMessage(data.getString("DateTime"), nikName+chatPartner, nikName, chatPartner, chatMessage.getMimetype(), chatMessage.getData());
                            chatMessages.add(new ChatMessage(nikName, chatPartner, data.getString("DateTime"), chatMessage.getMimetype(), chatMessage.getData(),0));
                            chatAdapter.notifyDataSetChanged();
                            listViewChat.setSelection(chatAdapter.getCount()-1);
                        }
                    } catch (Exception e) {
                        Info.show(ActivityChat.this, e.getMessage(), Info.Color.Red);
                        Log.d("LOG_ActivityChat", e.toString());
                    }
                }
                chatMessagesToSend.clear();
            }
            else {
                Log.d("LOG_ActivityChat", "no internet connection");
             }
        }
    }

    private void insertChatHistoriIntoDB(String fromDate) {
        try {
            JSONObject json = new JSONObject();
            json.put("Username", nikName);
            json.put("Password", password);
            json.put("Recipient", chatPartner);
            json.put("Offset", fromDate);
            JSONObject response = new NetworkHelper().execute("api/message/getoffset", json.toString()).get();

            if (response.getInt("MsgType") == 0) {
                Info.show(ActivityChat.this, response.getString("Info"), Info.Color.Red);
                Log.d("LOG_ActivityChat", response.toString());
            } else {
                JSONArray jarray = response.getJSONArray("Data");
                JSONObject jitem;
                for (int i = 0; i < jarray.length(); i++) {
                    jitem = jarray.getJSONObject(i);
                    String sender = jitem.getString("Sender");
                    String recipient = jitem.getString("Recipient");
                    String mimetype = jitem.getString("Mimetype");
                    String data = jitem.getString("Data");
                    String datetime = jitem.getString("DateTime");
                   if( MainActivity.DB.insertMessage(datetime, MainActivity.nikName+MainActivity.chatPartner, sender, recipient, mimetype, data) > 0){
                       chatMessages.add(new ChatMessage(sender, recipient, datetime, mimetype, data,0));
                       chatAdapter.notifyDataSetChanged();
                       listViewChat.setSelection(chatAdapter.getCount()-1);
                   }
                }
            }
        } catch (Exception e) {
            Log.d("LOG_ActivityChat", e.toString());
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            Bitmap bmp = Methods.getBitmap(ActivityChat.this, selectedImage, 1920, 1080);
            final ChatMessage message = new ChatMessage(nikName, chatPartner, "", "Image/*", Methods.bitmapToBase64(bmp),0);
            chatMessagesToSend.add(message);

            LayoutInflater inflater = LayoutInflater.from(ActivityChat.this);
            final View view = inflater.inflate(attachments, layoutAttachments, false);
            TextView tv = view.findViewById(R.id.TextViev_Attachments);
            tv.setText(Methods.getFileName(ActivityChat.this, selectedImage));
            LinearLayout lo = view.findViewById(R.id.ImageView_Attachments);
            lo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.setVisibility(View.GONE);
                    chatMessagesToSend.remove(message);
                }
            });
            layoutAttachments.addView(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void createImageBrowsingRequest() {
        if (ContextCompat.checkSelfPermission(ActivityChat.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ActivityChat.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 22);
        } else {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 1);
        }
    }
    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        final ChatMessage message = new ChatMessage(nikName, chatPartner, "","location/plain", location.getAltitude() + ":" + location.getLatitude() + ":" + location.getLongitude() + ":", 0);
        chatMessagesToSend.add(message);

        LayoutInflater inflater = LayoutInflater.from(ActivityChat.this);
        final View view = inflater.inflate(attachments, layoutAttachments, false);
        TextView tv = view.findViewById(R.id.TextViev_Attachments);
        tv.setText("Location: "  + location.getLatitude() + " | " + location.getLongitude());
        LinearLayout lo = view.findViewById(R.id.ImageView_Attachments);
        lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setVisibility(View.GONE);
                chatMessagesToSend.remove(message);
            }
        });
        layoutAttachments.addView(view);    }

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
