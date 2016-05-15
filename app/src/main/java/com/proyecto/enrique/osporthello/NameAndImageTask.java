package com.proyecto.enrique.osporthello;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by enrique on 15/05/16.
 */
public class NameAndImageTask extends AsyncTask<Void, Void, Void> {

    User userInfo;
    String email;
    TextView txtUsername;
    CircleImageView circleImageView;

    public NameAndImageTask(String email, TextView txt, CircleImageView img){
        this.email = email;
        this.txtUsername = txt;
        this.circleImageView = img;
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
            txtUsername.setText(userInfo.getFirstname()+ " "+userInfo.getLastname());
        circleImageView.setImageBitmap(ImageManager.stringToBitMap(userInfo.getImage()));
    }
}
