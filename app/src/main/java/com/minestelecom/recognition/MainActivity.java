package com.minestelecom.recognition;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.minestelecom.recognition.messaging.ServerRegistration;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.minestelecom.recognition.Config.REQUEST_IMAGE_CAPTURE;
import static com.minestelecom.recognition.Config.REQUEST_IMAGE_GALLERY;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Uri uriForImage = null;
    private Uri photoURI = null;
    String mCurrentPhotoPath = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> dispatchTakePictureIntent());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        /**
         * ANALYSE BTN
         */
        Button analyseBtn = (Button) findViewById(R.id.btnAnalyse);
        analyseBtn.setOnClickListener(v -> startAnalyse());


        /**
         * ASK PERMISSIONS
         */
        requestPermissions();

        // register messaging
        registerServerAndFCM();

        // delete cache, for crops...
        deleteCache(this.getApplicationContext());

        // reset variables
        resetVariables();
    }

    /**
     * Ask server for a security token.
     */
    private void registerServerAndFCM() {
        Config.FCM_TOKEN=FirebaseInstanceId.getInstance().getToken();
        ServerRegistration.sendServerToken(Config.FCM_TOKEN);

    }



    private void resetVariables() {
        mCurrentPhotoPath = null;
        uriForImage = null;
        photoURI = null;
    }

    private void requestPermissions() {
        Dexter.withActivity(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            dispatchTakePictureIntent();
        } else if (id == R.id.nav_gallery) {
            // Handle the gallery action
            dispatchGalleryPictureIntent();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_train) {

            startTraining();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Start request for train request.
     */
    private void startTraining() {

        String url = Config.SERVER_URL_BASE + "/" + "train";

        AsyncHttpGet getRequest = new AsyncHttpGet(url);
        getRequest.setTimeout(5000);

        AsyncHttpClient.getDefaultInstance().executeString(getRequest, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception ex, AsyncHttpResponse source, String result) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }

                MainActivity.this.runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Train request has been sent! -> " + result, Toast.LENGTH_LONG).show());

            }
        });

    }


    /**
     * CAMERA INTENT
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        resetVariables();
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.minestelecom.recognition.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Config.REQUEST_IMAGE_CAPTURE);
            }
        }

    }

    /**
     * GALLERY INTENT
     */
    private void dispatchGalleryPictureIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        resetVariables();
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, REQUEST_IMAGE_GALLERY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // reset uri image
        uriForImage = null;

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            processImageFromCamera(resultCode, data);
        } else if (requestCode == REQUEST_IMAGE_GALLERY) {
            processImageFromGallery(resultCode, data);
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }


        // to switch analyse button if needed
        switchAnalyseButton();

    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            uriForImage = Crop.getOutput(result);
            processImageWithURI(uriForImage);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Image coming from gallery
     *
     * @param resultCode
     * @param data
     */
    private void processImageFromGallery(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            beginCrop(data.getData());
        } else {
            Toast.makeText(getApplicationContext(), "An error has occurred...", Toast.LENGTH_LONG).show();
        }


    }

    /**
     * Current image.
     *
     * @param uri
     */
    public void processImageWithURI(Uri uri) {

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            ImageView imageView = (ImageView) findViewById(R.id.imgLoaded);
            imageView.setImageBitmap(bitmap);


            switchAnalyseButton();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "An error has occurred...", Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Process return from Camera.
     *
     * @param resultCode
     * @param data
     */
    private void processImageFromCamera(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras == null) {
                    return;
                }

                Bitmap imageBitmap = (Bitmap) extras.get("data");

                ImageView img = (ImageView) findViewById(R.id.imgLoaded);
                img.setImageBitmap(imageBitmap);
            } else {
                if (mCurrentPhotoPath != null) {
                    uriForImage = Uri.fromFile(new File(mCurrentPhotoPath));
                    beginCrop(uriForImage);
                } else
                    Toast.makeText(getApplicationContext(), "Data nul && picture nul", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(getApplicationContext(), "An error has occurred...", Toast.LENGTH_LONG).show();
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // remove on exit
        image.deleteOnExit();

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void startAnalyse() {
        Toast.makeText(getApplicationContext(), "Starting analyse...", Toast.LENGTH_LONG).show();


        // start server activity
        Intent uploadIntent = new Intent(this, UploadActivity.class);

      /*  if (mCurrentPhotoPath != null) {
            uploadIntent.putExtra("uri", mCurrentPhotoPath);
        }
        else {*/
            System.out.println("image uri : " + uriForImage);
            uploadIntent.putExtra("uri", uriForImage);


       // }

        startActivity(uploadIntent);


    }

    public String getImagePath(Uri uri) {
        if (uri == null) return null;

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {

            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();

            return path;
        }

        return uri.getEncodedPath();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    /**
     * To avoid null pointers.
     */
    private void switchAnalyseButton() {
        switchAnalyseButton(false);
    }

    private void switchAnalyseButton(boolean force) {
        Button btn = (Button) findViewById(R.id.btnAnalyse);

        if (force == true || uriForImage != null) {
            btn.setVisibility(View.VISIBLE);
        } else {
            btn.setVisibility(View.INVISIBLE);
        }
    }

}
