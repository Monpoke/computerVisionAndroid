package com.minestelecom.recognition.messaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.minestelecom.recognition.R;

public class MyMessagingService extends FirebaseMessagingService {
    public MyMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Toast.makeText(getApplicationContext(),"Coucou",Toast.LENGTH_SHORT).show();
        Log.d("MESSAGING", "Message data payload: " + remoteMessage.getData());


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

            Notification notification =  new Notification.Builder(this)
                    .setContentTitle("Notifi")
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setSmallIcon(R.drawable.ic_menu_camera)
                    .setAutoCancel(true).build();



            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(0, notification);

            Log.d("MESSAGING", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }
}
