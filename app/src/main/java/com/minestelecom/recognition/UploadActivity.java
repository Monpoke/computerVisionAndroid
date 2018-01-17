package com.minestelecom.recognition;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.callback.HttpConnectCallback;

import java.io.File;

public class UploadActivity extends AppCompatActivity {

    private TextView resultView;

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
        AsyncHttpPost post = new AsyncHttpPost(Config.SERVER_URL_BASE + "/" + "upload");
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addFilePart("file", new File(uri));
        post.setBody(body);
        AsyncHttpClient.getDefaultInstance().executeString(post, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception ex, AsyncHttpResponse source, String result) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }



            }
        }).setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                if(e==null){
                    System.out.println("Server says: " + result);
                    resultView.setText("server said: " + result);
                    // if it is filename, call it now...
                    callServerAnalyse(result);
                }
                else {
                    resultView.setText("Not uploaded for this reason... " + e.getMessage());
                }
            }
        });
    }


    /**
     * Appelle le serveur
     * @param pathFile
     */
    private void callServerAnalyse(String pathFile) {


        // url is the URL to download.
        AsyncHttpClient.getDefaultInstance().execute(Config.SERVER_URL_BASE + "/" + "analyse" + "/" + pathFile, new HttpConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, AsyncHttpResponse response) {
                System.out.println(response.code());
                System.out.println(response.message());

                if(response.code()==200){
                    resultView.setText("Résultat de prédiction:" + response.message());
                } else {
                    resultView.setText("Une erreur est survenue: " + response.message());
                }

            }
        });
    }
}
