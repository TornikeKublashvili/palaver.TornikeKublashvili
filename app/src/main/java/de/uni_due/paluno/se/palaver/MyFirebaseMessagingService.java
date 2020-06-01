package de.uni_due.paluno.se.palaver;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String action = "jason.broadcast.action";

    @Override
    public void onMessageReceived(RemoteMessage message)
    {
        super.onMessageReceived(message);

        if(MainActivity.nikName == null){
            return;
        }
        String sender = message.getData().get("sender");
        String preview = message.getData().get("preview");

        String user = MainActivity.nikName;
        if(!user.equals(sender))
        {
            createNotificationChannel();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "id")
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("Nachricht von " + sender)
                    .setContentText(preview)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.notify(1,builder.build());

            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = new Date();
            MainActivity.DB.insertUnreadMessages(MainActivity.nikName, sender, dateFormat.format(d));

            Intent intent = new Intent(action);
            intent.putExtra("sender",sender);
            sendBroadcast(intent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Channel";
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }
}
