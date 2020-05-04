package com.example.pushin_v5.imageTask;
/***********************************************************************
 This file is used to get the bitmap using URL to send an HTTP request
 then get the response stream which will be decoded into bitmap file.
 ***********************************************************************/

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetBitmapByURL extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... strings) {
        Bitmap myBitmap = null;
        try {
            URL url = new URL(strings[0]);
            System.out.println(strings[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            myBitmap = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myBitmap;
    }
}
