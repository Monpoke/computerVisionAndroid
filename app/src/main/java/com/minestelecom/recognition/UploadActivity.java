package com.minestelecom.recognition;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;

import java.io.File;
import java.util.concurrent.TimeoutException;

public class UploadActivity extends AppCompatActivity {

    private final UploadActivity activity;
    private TextView resultView;
    private boolean analyseInProgress = false;
    private Button btnReject;
    private Button btnValidate;

    private String gotClassName = null;
    private String gotFilename = null;

    UploadActivity() {
        this.activity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);


        gotClassName = null;
        gotFilename = null;

        // FIX
        resultView = (TextView) findViewById(R.id.result);


        // BTNS
        btnValidate = (Button) findViewById(R.id.validateResult);
        btnReject = (Button) findViewById(R.id.rejectResult);

        // hide
        btnValidate.setVisibility(View.INVISIBLE);
        btnReject.setVisibility(View.INVISIBLE);

        registerButtonsEvents();

        Intent intent = getIntent();
        String uri = intent.getExtras().getString("uri");
        startUpload(uri);
    }

    /**
     *
     */
    private void registerButtonsEvents() {



        btnValidate.setOnClickListener(view -> callValidation("yes"));
        btnReject.setOnClickListener(view -> callValidation("no"));


    }

    /**
     * Call url for validation.
     *
     * @param valid
     */
    private void callValidation(String valid) {
        String preFilename = gotFilename;
        String[] split = preFilename.split("_");
        split[0]= gotClassName;

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
                        btnReject.setVisibility(View.INVISIBLE);
                        btnValidate.setVisibility(View.INVISIBLE);
                    }


                });

            }
        });
    }

    private void startUpload(String uri) {
        String url = Config.SERVER_URL_BASE + "/" + "upload";

        System.out.println("sending file: " + uri);
        AsyncHttpPost post = new AsyncHttpPost(url);
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addFilePart("file", new File(uri));
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

        String uri = Config.SERVER_URL_BASE + "/" + "analyse" + "/" + pathFile;
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


                activity.runOnUiThread(() -> {
                    if (source.code() == 200) {
                        resultView.setText("Is it:\n" + result + "?");

                        // set global
                        gotClassName = result;

                        // set buttons visibles
                        btnReject.setVisibility(View.VISIBLE);
                        btnValidate.setVisibility(View.VISIBLE);

                        /**
                         * SHOW RESULT
                         */
                        ResultShowFragment fragment = new ResultShowFragment();
                        fragment.show(UploadActivity.this.getFragmentManager(),"UPLOAD");

                        fragment.setGotResult(result);
                        fragment.setPositive((dialogInterface, i) -> {
                            UploadActivity.this.callValidation("yes");
                        });

                        fragment.setNegative((dialogInterface, i) -> {
                            UploadActivity.this.callValidation("no");
                        });





                    } else {
                        resultView.setText("We could not analyse... \n" + result);
                    }
                });

                analyseInProgress = false;

            }
        });


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
}
