package com.proyecto.enrique.osporthello.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.CustomMapFragment;
import com.proyecto.enrique.osporthello.IndeterminateDialogTask;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ConfigurationActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private GoogleMap mapGeoSearch;
    private GoogleMap mapPrivacity;
    private ScrollView scrollView;
    private Spinner spinnerSport;
    private Switch switchGeoSearch;
    private Switch switchPrivacity;
    private Switch switchChat;
    private Switch switchFriends;
    private LinearLayout layoutGeoSearch;
    private LinearLayout layoutPrivacity;
    private Button btnSave;

    private Marker markerGeoSearch;
    private Marker markerPrivacity;
    private LatLng latLngGeoSearch;
    private LatLng latLngPrivacity;
    private Context context;

    private boolean geoSearchMarkerAdded = false;
    private boolean privacityMarkerAdded = false;
    private int sportType = 0;
    private static final int ZOOM = 6;
    private static final String PREFERENCES_FILE = "osporthello_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        context = this;
        spinnerSport = (Spinner)findViewById(R.id.spinnerSport);
        switchGeoSearch = (Switch)findViewById(R.id.swGeoSearch);
        switchPrivacity = (Switch)findViewById(R.id.swPrivacity);
        switchChat = (Switch)findViewById(R.id.swChatNotifications);
        switchFriends = (Switch)findViewById(R.id.swFriendsNotifications);
        layoutGeoSearch = (LinearLayout)findViewById(R.id.layoutGeoSearch);
        layoutPrivacity = (LinearLayout)findViewById(R.id.layoutPrivacity);
        btnSave = (Button)findViewById(R.id.btnEditConfiguration);

        if(mapGeoSearch == null) {
            mapGeoSearch = ((CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapGeoSearch)).getMap();
            mapGeoSearch.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mapGeoSearch.getUiSettings().setZoomControlsEnabled(true);
            scrollView = (ScrollView) findViewById(R.id.scrollView);

            ((CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapGeoSearch))
                    .setListener(new CustomMapFragment.OnTouchListener() {
                        @Override
                        public void onTouch() {
                            scrollView.requestDisallowInterceptTouchEvent(true);
                        }
                    });
        }
        if(mapPrivacity == null) {
            mapPrivacity = ((CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPrivacity)).getMap();
            mapPrivacity.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mapPrivacity.getUiSettings().setZoomControlsEnabled(true);
            scrollView = (ScrollView) findViewById(R.id.scrollView);

            ((CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPrivacity))
                    .setListener(new CustomMapFragment.OnTouchListener() {
                        @Override
                        public void onTouch() {
                            scrollView.requestDisallowInterceptTouchEvent(true);
                        }
                    });
        }

        initializeSpinner();
        initializeControls();
        initializeMaps();
        switchGeoSearch.setOnCheckedChangeListener(this);
        switchPrivacity.setOnCheckedChangeListener(this);
        switchChat.setOnCheckedChangeListener(this);
        switchFriends.setOnCheckedChangeListener(this);
        btnSave.setOnClickListener(this);

        // To show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeControls() {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        switchGeoSearch.setChecked((sharedPref.getInt("geosearch", 0) == 0)?false:true);
        switchPrivacity.setChecked((sharedPref.getInt("privacity", 0) == 0)?false:true);
        float latGeo = sharedPref.getFloat("geolat", (float)40.416750);
        float lonGeo = sharedPref.getFloat("geolon", (float)-3.703813);
        spinnerSport.setSelection(sharedPref.getInt("sporttype", 0));
        float latPrivacity = sharedPref.getFloat("privacitylat", (float)40.416750);
        float lonPrivacity = sharedPref.getFloat("privacitylon", (float)-3.703813);
        switchChat.setChecked((sharedPref.getInt("chatnotifications", 1) == 0)?false:true);
        switchFriends.setChecked((sharedPref.getInt("friendsnotification", 1) == 0)?false:true);

        if(switchGeoSearch.isChecked())
            layoutGeoSearch.setVisibility(View.VISIBLE);
        if(switchPrivacity.isChecked())
            layoutPrivacity.setVisibility(View.VISIBLE);

        if(!(latGeo == 0 && lonGeo == 0))
            latLngGeoSearch = new LatLng(latGeo, lonGeo);
        if(!(latPrivacity == 0 && lonPrivacity == 0))
            latLngPrivacity = new LatLng(latPrivacity, lonPrivacity);

        if(sharedPref.contains("geolat") || sharedPref.contains("geolon"))
            this.geoSearchMarkerAdded = true;
        if(sharedPref.contains("privacitylat") || sharedPref.contains("privacitylon"))
            this.privacityMarkerAdded = true;
    }

    private void initializeMaps() {
        try {
            markerGeoSearch = updateMapView(latLngGeoSearch, mapGeoSearch, geoSearchMarkerAdded, markerGeoSearch);
            markerPrivacity = updateMapView(latLngPrivacity, mapPrivacity, privacityMarkerAdded, markerPrivacity);

            mapGeoSearch.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if(markerGeoSearch != null)
                        markerGeoSearch.remove();
                    markerGeoSearch = mapGeoSearch.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            });
            mapPrivacity.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if(markerPrivacity != null)
                        markerPrivacity.remove();
                    markerPrivacity = mapPrivacity.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            });
        }
        catch (SecurityException e){
            Log.e("MAP_PERMISSIONS", "Error!!: Al inicializar el mapa");}
    }

    /**
     *
     * @param latLng
     */
    private Marker updateMapView(LatLng latLng, GoogleMap map, boolean addMarker, Marker marker) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
        if(addMarker)
            marker = map.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        return marker;
    }

    /**
     *
     */
    private void initializeSpinner() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(getString(R.string.cyclist));
        list.add(getString(R.string.runner));
        list.add(getString(R.string.others));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,list);
        spinnerSport.setAdapter(adapter);

        spinnerSport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sportType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(switchGeoSearch.isChecked() && markerGeoSearch == null){
            Toast.makeText(context, "Si activas la GeoBúsqueda, debes elegir un punto en el mapa", Toast.LENGTH_LONG).show();
            return;
        }
        if(switchPrivacity.isChecked() && markerPrivacity == null){
            Toast.makeText(context, "Si activas la Zona de Privacidad, debes elegir un punto en el mapa", Toast.LENGTH_LONG).show();
            return;
        }

        btnSave.setEnabled(false);
        final IndeterminateDialogTask progressDialog = new IndeterminateDialogTask(ConfigurationActivity.this, getString(R.string.saving_configuration));
        progressDialog.execute();

        // Save current configuration
        JSONObject json = new JSONObject();
        try {
            json.put("email", MainActivity.USER_ME.getEmail());
            json.put("geosearch", (switchGeoSearch.isChecked())?1:0);
            json.put("privacity", (switchPrivacity.isChecked())?1:0);
            if(switchGeoSearch.isChecked()) {
                json.put("geolat", (markerGeoSearch != null) ? markerGeoSearch.getPosition().latitude : 0);
                json.put("geolon", (markerGeoSearch != null) ? markerGeoSearch.getPosition().longitude : 0);
            }
            else{
                json.put("geolat", 0);
                json.put("geolon", 0);
            }
            json.put("sporttype", sportType);
            if(switchPrivacity.isChecked()) {
                json.put("privacitylat", (markerPrivacity != null) ? markerPrivacity.getPosition().latitude : 0);
                json.put("privacitylon", (markerPrivacity != null) ? markerPrivacity.getPosition().longitude : 0);
            }
            else{
                json.put("privacitylat", 0);
                json.put("privacitylon", 0);
            }
            json.put("chatnotifications", (switchChat.isChecked())?1:0);
            json.put("friendsnotification", (switchFriends.isChecked()?1:0));
        } catch (Exception e){}

        RequestParams param = new RequestParams();
        param.put("userconf", json.toString());
        ApiClient.postMyConfiguration("api/configuration", param, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                btnSave.setEnabled(true);
                progressDialog.cancel(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                btnSave.setEnabled(true);
                progressDialog.cancel(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                btnSave.setEnabled(true);
                progressDialog.cancel(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        Toast.makeText(context, "Configuración actualizada", Toast.LENGTH_SHORT).show();
                        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("geosearch", (switchGeoSearch.isChecked())?1:0);
                        editor.putInt("privacity", (switchPrivacity.isChecked())?1:0);
                        if(markerGeoSearch != null) {
                            editor.putFloat("geolat", (float) markerGeoSearch.getPosition().latitude);
                            editor.putFloat("geolon", (float) markerGeoSearch.getPosition().longitude);
                        }
                        editor.putInt("sporttype", sportType);
                        if(markerPrivacity != null) {
                            editor.putFloat("privacitylat", (float) markerPrivacity.getPosition().latitude);
                            editor.putFloat("privacitylon", (float) markerPrivacity.getPosition().longitude);
                        }
                        editor.putInt("chatnotifications", (switchChat.isChecked())?1:0);
                        editor.putInt("friendsnotification", (switchFriends.isChecked()?1:0));

                        if(!switchGeoSearch.isChecked()) {
                            editor.remove("geolat");
                            editor.remove("geolon");
                        }
                        if(!switchPrivacity.isChecked()){
                            editor.remove("privacitylat");
                            editor.remove("privacitylon");
                        }
                        editor.apply();
                        initializeControls();
                        initializeMaps();
                    }
                    btnSave.setEnabled(true);
                    progressDialog.cancel(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    btnSave.setEnabled(true);
                    progressDialog.cancel(true);
                }
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.swGeoSearch:
                if(isChecked){
                    layoutGeoSearch.setVisibility(View.VISIBLE);
                }
                else{
                    layoutGeoSearch.setVisibility(View.GONE);
                    geoSearchMarkerAdded = false;
                }
                break;
            case R.id.swPrivacity:
                if(isChecked){
                    layoutPrivacity.setVisibility(View.VISIBLE);
                }
                else{
                    layoutPrivacity.setVisibility(View.GONE);
                    privacityMarkerAdded = false;
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
