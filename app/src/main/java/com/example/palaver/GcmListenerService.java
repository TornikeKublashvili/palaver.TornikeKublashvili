package com.example.palaver;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        try {
            //TODO
            // It rings but it shows no Notifications + It works only if the App is running

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
            Log.d("LOG_GcmListenerService", "Receive Messsage From " + from + " Bundle " + bundle.toString());
        } catch (Exception e) {
            Log.d("LOG_GcmListenerService", e.toString());
        }
    }
}
