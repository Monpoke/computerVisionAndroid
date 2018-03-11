package com.minestelecom.recognition;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.minestelecom.recognition.messaging.MyMessagingService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

public class UploadActivity extends AppCompatActivity {

    private final UploadActivity activity;
    private TextView resultView;
    private boolean analyseInProgress = false;

    private String gotClassName = null;
    private String gotFilename = null;


    // for communications services
    MyResultReceiver resultReceiver;


    public UploadActivity() {
        this.activity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // set receiver to messaging service
        resultReceiver = new MyResultReceiver(null);
        MyMessagingService.setResultReceiver(resultReceiver);


        gotClassName = null;
        gotFilename = null;

        // FIX
        resultView = (TextView) findViewById(R.id.result);



        //registerButtonsEvents();

        Intent intent = getIntent();
        Uri uri = (Uri) intent.getExtras().get("uri");


        startUpload(uri);
    }


    /**
     * Call url for validation.
     *
     * @param valid
     */
    private void callValidation(String valid) {
        String preFilename = gotFilename;
        String[] split = preFilename.split("_");
        split[0] = gotClassName;

        String filename = split[0] + "_" + split[1];

        String url = Config.SERVER_URL_BASE + "/" + "validate" + "/" + valid + "/" + filename;

        AsyncHttpGet getRequest = new AsyncHttpGet(url);
        getRequest.setTimeout(5000);

        AsyncHttpClient.getDefaultInstance().executeString(getRequest, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception ex, AsyncHttpResponse source, String result) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }

                UploadActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Validated! -> " + result, Toast.LENGTH_SHORT).show();
                    }


                });

            }
        });
    }

    private void startUpload(Uri uri) {
        String url = Config.SERVER_URL_BASE + "/" + "upload";


        File file = new File(uri.getPath());
        if (file.exists()) {
            System.out.println("File existing: " + file.getPath() + " > " + file.isFile());
        }

        System.out.println("sending file: " + uri);
        AsyncHttpPost post = new AsyncHttpPost(url);
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addFilePart("file", file);
        post.setBody(body);
        resultView.setText("Upload in progress to: " + url);
        post.setTimeout(60000);

        // set in progress
        //  analyseInProgress=true;

        // Lambda expression

        AsyncHttpClient.getDefaultInstance().executeString(post, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception ex, AsyncHttpResponse source, String result) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                activity.runOnUiThread(() -> {

                    if (ex instanceof TimeoutException) {
                        System.out.println("Timeout for upload request...");
                    }


                    if (source.code() == 200) {

                        System.out.println("Server says: " + result);
                        resultView.setText("Resource has been uploaded under " + result + "\n" +
                                "Please wait few minutes for result...");
                    } else {
                        resultView.setText("Not uploaded for this reason... " + result);
                    }

                });

                if (source.code() == 200) {

                    gotFilename = result;
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

        /**
         * Gives TOKEN IN URL
         */
        String uri = Config.SERVER_URL_BASE + "/" + "analyse" + "/" + pathFile
                + "?fcm=" + Config.FCM_TOKEN + "&token=" + Config.SERVER_TOKEN;


        AsyncHttpGet asyncHttpGet = new AsyncHttpGet(uri);
        asyncHttpGet.setTimeout(60000 * 10);
        System.out.println("Calling url: " + uri);
        AsyncHttpClient.getDefaultInstance().executeString(asyncHttpGet, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception ex, AsyncHttpResponse source, String result) {
                if (source == null) {

                    if (ex instanceof TimeoutException) {
                        System.out.println("Timeout for analyse request...");
                    }

                    ex.printStackTrace();
                    return;
                }
                System.out.println(source.code());
                System.out.println(source.message());

                analyseInProgress = false;

            }
        });

    }

    public static void ff() {


    }


    @Override
    public void onBackPressed() {
        if (analyseInProgress) {
            Toast.makeText(activity, "Analyse is still in progress.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            super.onBackPressed();
        }
    }


    /**
     * This has to be called on prediction result.
     *
     * @param predictionResult
     */
    protected void onPredictionReceived(String predictionResult) {
        activity.runOnUiThread(() -> {
            resultView.setText("");
            resultView.setVisibility(View.INVISIBLE);
            findViewById(R.id.resultProgress).setVisibility(View.GONE);

            // set global
            gotClassName = predictionResult;

            // set buttons visibles
       //     btnReject.setVisibility(View.VISIBLE);
         //   btnValidate.setVisibility(View.VISIBLE);

            /**
             * SHOW RESULT
             */
            ResultShowFragment fragment = new ResultShowFragment();
            fragment.setCancelable(false);
            fragment.show(UploadActivity.this.getFragmentManager(), "UPLOAD");

            fragment.setGotResult(predictionResult);
            fragment.setPositive((dialogInterface, i) -> {
                UploadActivity.this.callValidation("yes");

                UploadActivity.this.runOnUiThread(() -> UploadActivity.this.finish());
            });

            fragment.setNegative((dialogInterface, i) -> {
                UploadActivity.this.callValidation("no");

                UploadActivity.this.runOnUiThread(() -> UploadActivity.this.finish());
            });

        });
    }




    /**
     * HANDLE RETURN RESULTS FROM PREDICTIONS
     */
    private class MyResultReceiver extends ResultReceiver {
        public MyResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode == 1) {

                String prediction = resultData.getString("prediction");
                UploadActivity.this.onPredictionReceived(prediction);

            }

        }
    }


}
