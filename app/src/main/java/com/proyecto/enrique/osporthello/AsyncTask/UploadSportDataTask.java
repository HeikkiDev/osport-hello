package com.proyecto.enrique.osporthello.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.SportActivityInfo;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: AsyncTask que crea un objeto JSON con los datos de un entrenamiento y lo sube al servidor externo.
 */

public class UploadSportDataTask extends AsyncTask<SportActivityInfo, Void, Void> {

    public interface onFinishUpload{
        void onFinish();
    }

    Context context;
    onFinishUpload myInterface;

    public UploadSportDataTask(Context context, onFinishUpload interf){
        this.context = context;
        this.myInterface = interf;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(context.getApplicationContext(), R.string.saving_work_data,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected Void doInBackground(SportActivityInfo... params) {
        SportActivityInfo activityInfo = params[0];

        try {
            JSONObject json = new JSONObject();
            json.put("email", MainActivity.USER_ME.getEmail());
            json.put("sportType", activityInfo.getSportType());
            json.put("typeName", activityInfo.getName());
            json.put("distanceUnits", activityInfo.getDistanceUnits());
            json.put("speedUnits", activityInfo.getSpeedUnits());
            json.put("avgSpeed", activityInfo.getAvgSpeed());
            json.put("distance", activityInfo.getDistanceKms());
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
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(context.getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Log.e("SPORT_DATA", "Error");
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(context.getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.e("SPORT_DATA", "Error");
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(context.getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.e("SPORT_DATA","Success");
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(context.getApplicationContext(), R.string.workout_saved,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (JSONException e) {Log.e("NEW_ACTIVITY", e.getMessage());}

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        myInterface.onFinish();
    }
}
