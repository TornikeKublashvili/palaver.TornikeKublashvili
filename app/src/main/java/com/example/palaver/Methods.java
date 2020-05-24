package com.example.palaver;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.graphics.BitmapFactory.decodeStream;

class Methods {

    static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    static Bitmap base64ToBitmap(String b64) {
        try{
            byte [] encodeByte = Base64.decode(b64,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        }
        catch(Exception e){
            Log.d("LOG_ActivityChat", e.toString());
            return null;
        }
    }
    // loads bitmap from uri
    static Bitmap getBitmap(Context context, Uri selectedImage, int targetWidth, int targetHeight) {
        ContentResolver resolver = context.getContentResolver();
        InputStream is = null;
        try {
            is = resolver.openInputStream(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);

        try {
            is = resolver.openInputStream(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = false;
        return decodeStream(is, null, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int bitmapWidth, int bitmapHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > bitmapHeight || width > bitmapWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > bitmapHeight
                    && (halfWidth / inSampleSize) > bitmapWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
