package com.minestelecom.recognition;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.callback.HttpConnectCallback;

import java.io.File;
import java.util.concurrent.TimeoutException;

public class UploadActivity extends AppCompatActivity {

    private final UploadActivity activity;
    private TextView resultView;

    UploadActivity() {
        this.activity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // FIX
        resultView = (TextView) findViewById(R.id.result);


        Intent intent = getIntent();
        String uri = intent.getExtras().getString("uri");
        startUpload(uri);
    }

    private void startUpload(String uri) {
        String url = Config.SERVER_URL_BASE + "/" + "upload";

        AsyncHttpPost post = new AsyncHttpPost(url);
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addFilePart("file", new File(uri));
        post.setBody(body);
        resultView.setText("Upload in progress to: "+url);
        post.setTimeout(60000);


        // Lambda expression

        AsyncHttpClient.getDefaultInstance().executeString(post, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception ex, AsyncHttpResponse source, String result) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                activity.runOnUiThread(() -> {

                    if(ex instanceof TimeoutException){
                        System.out.println("Timeout for upload request...");
                    }


                    if (source.code() == 200) {

                        System.out.println("Server says: " + result);
                        resultView.setText("server said: " + result);
                    } else {
                        resultView.setText("Not uploaded for this reason... " + result);
                    }

                });

                if(source.code()==200){
                    // if it is filename, call it now...
                    callServerAnalyse(result);
                }


            }
        });
    }


    /**
     * Appelle le serveur
     *
     * @param pathFile
     */
    private void callServerAnalyse(String pathFile) {

        String uri= Config.SERVER_URL_BASE + "/" + "analyse" + "/" + pathFile;
        AsyncHttpGet asyncHttpGet = new AsyncHttpGet(uri);
        asyncHttpGet.setTimeout(60000*10);


        // Lambda expression
        AsyncHttpClient.getDefaultInstance().execute(asyncHttpGet, (ex, response) -> {

            if(response==null){

                if(ex instanceof TimeoutException){
                    System.out.println("Timeout for analyse request...");
                }

                ex.printStackTrace();
                return;
            }
            System.out.println(response.code());
            System.out.println(response.message());

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (response.code() == 200) {
                        resultView.setText("Résultat de prédiction:" + response.message() + response.getDataCallback().);
                    } else {
                        resultView.setText("Une erreur est survenue: " + response.message());
                    }
                }
            });
        });


    }
}
