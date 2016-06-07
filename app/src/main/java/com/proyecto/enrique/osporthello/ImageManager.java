package com.proyecto.enrique.osporthello;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.User;

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
public class ImageManager {

    Context context;

    public ImageManager(Context context){
        this.context = context;
    }

    public String saveToInternalStorage(Bitmap bitmapImage, String nameFile){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(nameFile, Context.MODE_PRIVATE);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 0, fos);

            uploadStringImageToRemote(bitmapImage);
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
            if(b != null)
                imageView.setImageBitmap(b);
        }
        catch (FileNotFoundException|OutOfMemoryError e)
        {
            e.printStackTrace();
        }
    }

    public void uploadStringImageToRemote(Bitmap bitmap) {
        Bitmap resizedImage = Bitmap.createScaledBitmap(bitmap,150,128, true);
        UploadImageTask imageTask = new UploadImageTask();
        imageTask.execute(resizedImage);
    }

    /**
     * Bitmap to String 64 base enconded
     * @param bmp Bitmap
     * @return
     */
    private static String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 0, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    /**
     * String 64 base enconded to Bitmap
     * @param encodedString
     * @return bitmap (from given string)
     */
    public static Bitmap stringToBitMap(String encodedString){
        try{
            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(OutOfMemoryError|Exception e){
            e.getMessage();
            return null;
        }
    }

    private class UploadImageTask extends AsyncTask <Bitmap, Void, Void>{

        @Override
        protected Void doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            User user = MainActivity.USER_ME;

            try {
                JSONObject json = new JSONObject();
                json.put("email", user.getEmail());
                json.put("image", getStringImage(bitmap));
                RequestParams param = new RequestParams();
                param.put("user", json.toString());

                SyncHttpClient client = new SyncHttpClient(true, 80, 443);
                client.setTimeout(10000);
                client.put(MainActivity.HOST + "api/users/image/" + user.getApiKey(), param, new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                        Log.e("IMAGE", "Error");
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.e("IMAGE", "Success");
                    }
                });
            }
            catch (JSONException e){
                e.printStackTrace();
            }

            return null;
        }
    }
}
