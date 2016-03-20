package com.proyecto.enrique.osporthello;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by enrique on 18/03/16.
 */
public class StorageImage {

    Context context;

    public StorageImage(Context context){
        this.context = context;
    }

    public String saveToInternalStorage(Bitmap bitmapImage, String nameFile){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(nameFile, Context.MODE_PRIVATE);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return nameFile;
    }

    public void loadImageFromStorage(String name, ImageView imageView)
    {
        try {
            FileInputStream fis = context.openFileInput(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            imageView.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void uploadStringImageToRemote(Bitmap bitmap) {
        UploadImageTask imageTask = new UploadImageTask();
        imageTask.execute(bitmap);
    }

    private String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private class UploadImageTask extends AsyncTask <Bitmap, Void, Void>{

        @Override
        protected Void doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];

            User user = MainActivity.USER_ME;
            RequestParams param = null;
            try {
                JSONObject json = new JSONObject();
                json.put("email", user.getEmail());
                json.put("image", getStringImage(bitmap));
                param = new RequestParams();
                param.put("user", json.toString());
            } catch (JSONException e) {e.printStackTrace();}

            SyncHttpClient client = new SyncHttpClient(true, 80, 443);
            client.setTimeout(10000);
            client.put(MainActivity.HOST+"api/users/image/"+user.getApiKey(), param, new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("IMAGE","Error");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.e("IMAGE","Success");
                }
            });

            return null;
        }
    }
}
