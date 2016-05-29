package com.proyecto.enrique.osporthello.AsyncTask;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ImageManager;
import com.proyecto.enrique.osporthello.Interfaces.UserInfoInterface;
import com.proyecto.enrique.osporthello.Models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by enrique on 15/05/16.
 */
public class NameAndImageTask extends AsyncTask<Void, Void, Void> {

    User userInfo;
    String email;
    TextView txtUsername;
    ImageView imageView;
    int index;
    UserInfoInterface infoInterface;

    public NameAndImageTask(String email, TextView txt, ImageView image, int i, UserInfoInterface info){
        this.email = email;
        this.txtUsername = txt;
        this.imageView = image;
        this.index = i;
        this.infoInterface = info;
        userInfo = new User();
    }

    @Override
    protected Void doInBackground(Void... aVoid) {
        try {
            final User user = MainActivity.USER_ME;
            SyncHttpClient client = new SyncHttpClient(true, 80, 443);
            client.setTimeout(10000);
            client.get(MainActivity.HOST+"api/users/name-image/" + email + "/" + user.getApiKey(), new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("USER_INFO", "ERROR!!");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Log.e("USER_INFO", "ERROR!!");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.e("USER_INFO", "ERROR!!");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (!response.getString("data").equals("null")) {
                            userInfo = AnalyzeJSON.analyzeUserNameImage(response);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e){}

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(txtUsername != null)
            txtUsername.setText(userInfo.getFirstname()+" "+((userInfo.getLastname()!=null)?userInfo.getLastname():""));
        if(imageView != null)
            imageView.setImageBitmap(ImageManager.stringToBitMap(userInfo.getImage()));
        if(infoInterface != null)
            infoInterface.onInfoUserChanges(userInfo, index);
    }
}
