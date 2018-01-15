package com.minestelecom.recognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;

/**
 * Created by Pierre on 09/01/2018.
 */

class ServerInteraction implements Runnable{
    private final MainActivity mainActivity;
    private final Bitmap bitmap;
    private EditText editText;

    public ServerInteraction(MainActivity mainActivity, Bitmap bitmap, EditText editText) {

        this.mainActivity = mainActivity;
        this.bitmap = bitmap;
        this.editText = editText;
    }

    @Override
    public void run() {

        // get base64 for images
        //encode image to base64 string
        String hash = getBitmapBase64();

        editText.setText(hash);


    }

    private String getBitmapBase64() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}
