package com.minestelecom.recognition.messaging;

import android.util.Log;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.minestelecom.recognition.Config;

/**
 * Created by Pierre on 11/03/2018.
 */

public class ServerRegistration {

    /**
     * Send a token to the server.
     *
     * @param token
     */
    public static void sendServerToken(String token) {
        if (token == null || token.isEmpty()) {
            Log.e("ServerRegistration", "FCM token is empty or null...");
            return;
        }

        AsyncHttpGet serverRequest = new AsyncHttpGet(Config.SERVER_URL_BASE + "/tokregister/" + token);
        AsyncHttpClient.getDefaultInstance().executeString(serverRequest, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, String result) {
                Log.d("INFO", "token sent to server: " + result);
                Config.SERVER_TOKEN = result;
            }
        });
    }
}
