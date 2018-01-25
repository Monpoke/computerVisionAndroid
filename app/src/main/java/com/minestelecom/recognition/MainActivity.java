package com.minestelecom.recognition;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.DexterBuilder.MultiPermissionListener;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.jar.Manifest;

import static com.minestelecom.recognition.Config.REQUEST_IMAGE_CAPTURE;
import static com.minestelecom.recognition.Config.REQUEST_IMAGE_GALLERY;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Uri uriForImage = null;
    private  Uri photoURI = null;
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


        // reset variables
        resetVariables();
    }

    private void resetVariables(){
        mCurrentPhotoPath=null;
        uriForImage=null;
        photoURI=null;
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

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        uriForImage=null;

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            processImageFromCamera(resultCode, data);
        } else if (requestCode == REQUEST_IMAGE_GALLERY) {
            processImageFromGallery(resultCode, data);
        }


        // to switch analyse button if needed
        switchAnalyseButton();

    }

    /**
     * Image coming from gallery
     *
     * @param resultCode
     * @param data
     */
    private void processImageFromGallery(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            uriForImage = data.getData();
            processImageWithURI(uriForImage);
        } else {
            Toast.makeText(getApplicationContext(), "An error has occurred...", Toast.LENGTH_LONG).show();
        }


    }

    /**
     * Current image.
     * @param uri
     */
    public void processImageWithURI(Uri uri){

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

           if(data!=null) {
               Bundle extras = data.getExtras();
               Bitmap imageBitmap = (Bitmap) extras.get("data");

               ImageView img = (ImageView) findViewById(R.id.imgLoaded);
               img.setImageBitmap(imageBitmap);
           } else {
               if(mCurrentPhotoPath!=null) {
                   uriForImage=Uri.fromFile(new File(mCurrentPhotoPath));
                   processImageWithURI(uriForImage);
               }
               else
                   Toast.makeText(getApplicationContext(),"Data nul && picture nul",Toast.LENGTH_SHORT).show();

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

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void startAnalyse() {
        Toast.makeText(getApplicationContext(), "Starting analyse...", Toast.LENGTH_LONG).show();


        // start server activity
        Intent uploadIntent = new Intent(this, UploadActivity.class);

        if(mCurrentPhotoPath!=null){
            uploadIntent.putExtra("uri", mCurrentPhotoPath);
        }
        else {
            uploadIntent.putExtra("uri", getImagePath(uriForImage));
        }

        startActivity(uploadIntent);


    }

    public String getImagePath(Uri uri) {
        if (uri == null) return null;

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
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
