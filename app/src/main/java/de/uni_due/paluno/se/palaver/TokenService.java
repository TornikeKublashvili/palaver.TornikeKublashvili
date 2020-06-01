package de.uni_due.paluno.se.palaver;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import org.json.JSONObject;
import java.io.IOException;

public class TokenService extends IntentService{

    public TokenService() {
        super("TokenService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            String token = instanceID.getToken(getString(R.string.tocken_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendTokenToServer(token);
        } catch (IOException e) {
            Log.d("LOG_TokenService", e.toString());
        }
    }

    public void sendTokenToServer(String gcmID){
        if (Info.isNetworkAvailable(TokenService.this)) {
            try {
                JSONObject json = new JSONObject();
                json.put("Username", MainActivity.nikName);
                json.put("Password", MainActivity.password);
                json.put("PushToken", gcmID);

                JSONObject response = new NetworkHelper().execute("/api/user/pushtoken", json.toString()).get();
                Log.d("LOG_TokenService", response.toString());

            } catch (Exception e) {
                Log.d("LOG_TokenService", e.toString());
            }
        } else {
            Log.d("LOG_TokenService", "no internet connection");
        }
    }
}
