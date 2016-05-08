package com.proyecto.enrique.osporthello;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.SportActivityInfo;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by enrique on 7/05/16.
 */
public class UploadSportDataTask extends AsyncTask<SportActivityInfo, Void, Void> {

    Context context;
    ProgressDialog progressDialog;

    public UploadSportDataTask(Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.saving_work_data));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(SportActivityInfo... params) {
        SportActivityInfo activityInfo = params[0];

        try {
            JSONObject json = new JSONObject();
            json.put("email", MainActivity.USER_ME.getEmail());
            json.put("sportType", activityInfo.getSportType());
            json.put("distanceUnits", activityInfo.getDistanceUnits());
            json.put("speedUnits", activityInfo.getSpeedUnits());
            json.put("avgSpeed", activityInfo.getAvgSpeed());
            json.put("distance", activityInfo.getDistanceMetres());
            json.put("duration", activityInfo.getDurationMillis());
            json.put("calories", activityInfo.getCalories());
            json.put("geo_points", activityInfo.getEncodedPointsList());

            RequestParams param = new RequestParams();
            param.put("sport_data", json.toString());

            SyncHttpClient client = new SyncHttpClient(true, 80, 443);
            client.setTimeout(10000);
            client.post(MainActivity.HOST+"api/activity/new/"+MainActivity.USER_ME.getApiKey(), param, new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("SPORT_DATA", "Error");
                    progressDialog.dismiss();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.e("SPORT_DATA","Success");
                    progressDialog.dismiss();
                    Toast.makeText(context.getApplicationContext(),"Workout saved correctly",Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {Log.e("NEW_ACTIVITY", e.getMessage());}

        return null;
    }
}
