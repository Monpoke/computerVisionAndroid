package com.minestelecom.recognition.messaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.minestelecom.recognition.R;
import com.minestelecom.recognition.UploadActivity;

import java.util.Map;

import javax.xml.transform.Result;

public class MyMessagingService extends FirebaseMessagingService {

    private static ResultReceiver resultReceiver = null;


    public MyMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("MESSAGING", "Message data payload: " + remoteMessage.getData());


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

            Notification notification =  new Notification.Builder(this)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setSmallIcon(R.drawable.ic_menu_camera)
                    .setAutoCancel(true).build();



            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(0, notification);

            Log.d("MESSAGING", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        } else {

            if(resultReceiver==null){
                return;
            }

            // action to process?
            Map<String, String> data = remoteMessage.getData();
            if (data.get("action") != null){

                switch(data.get("action")){
                    case "analyse":
                        // forward to analyse

                        Bundle bundle = new Bundle();
                        bundle.putString("prediction",data.get("prediction"));
                        resultReceiver.send(Integer.valueOf(data.get("code")),bundle);

                        break;
                }


            }

        }

    }

    public static void setResultReceiver(ResultReceiver rr) {
        resultReceiver = rr;
    }
}
