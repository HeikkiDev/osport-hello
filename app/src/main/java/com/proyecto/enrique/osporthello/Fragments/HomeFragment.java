package com.proyecto.enrique.osporthello.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Adapters.ChooseActivityAdapter;
import com.proyecto.enrique.osporthello.Models.RowActivity;
import com.proyecto.enrique.osporthello.Models.SportActivityInfo;
import com.proyecto.enrique.osporthello.AsyncTask.MyTimerTask;
import com.proyecto.enrique.osporthello.R;
import com.proyecto.enrique.osporthello.AsyncTask.UploadSportDataTask;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Fragment que se muestra al inicio de la aplicación por defecto. Aquí se registran y guardan los entrenamientos
 * que realice el usuario.
 */

public class HomeFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, LocationListener, UploadSportDataTask.onFinishUpload  {

    private LinearLayout rootLayoutHome;
    private LinearLayout layoutInfoWork;
    private LinearLayout layoutStartWorkout;
    private LinearLayout layoutStopPause;
    private LinearLayout layoutChooseActivity;
    private LinearLayout layoutChooseDistance;
    private LinearLayout layoutChooseSpeed;
    private TextView txtChooseActivity;
    private TextView txtChooseDistance;
    private TextView txtChooseSpeed;
    private TextView txtDuration;
    private TextView txtTitleSpeed;
    private TextView txtTitleDistance;
    private TextView txtTitleCalories;
    private TextView txtSpeed;
    private TextView txtDistance;
    private TextView txtCalories;
    private ImageView iconSpeed;
    private ImageView iconDistance;
    private ImageView iconCalories;
    private Button btnStartWorkout;
    private Button btnStop;
    private Button btnPause;

    private UploadSportDataTask.onFinishUpload myInterface;
    private GoogleMap mMap;
    private Marker marker;
    private Context context;
    private AlertDialog alertDialog;
    private Timer timer;
    private MyTimerTask myTimerTask;
    private Location previousLocation;
    private LocationManager locationManager;
    private PolylineOptions polyline;
    private LatLngBounds radiusBounds = null;

    private long timeStart = 0;
    private long timePause = 0;
    private double distance = 0d;
    private double avgSpeed = 0d;
    private int calories = 0;
    private double factorSpeed = 1;
    private double factorDistance = 1;
    private long countAvgSpeed = 0;
    private long previousTime = 0;
    private double typeFactor = 0.048; // Cycling default
    private boolean isTimerRunning = false;
    private boolean isTimerPaused = false;

    private final int KITKAT = 19;
    private final int LOCATION_CODE = 1;
    private final double KMS_TO_MILES = 0.621371;
    private final int ZOOM = 16;
    private final int RADIUS_METERS = 500;
    private static final String PREFERENCES_FILE = "osporthello_settings";

    public HomeFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        context = getContext();
        rootLayoutHome = (LinearLayout)view.findViewById(R.id.rootLayoutHome);
        layoutInfoWork = (LinearLayout)view.findViewById(R.id.layoutInfoWorkout);
        layoutStartWorkout= (LinearLayout)view.findViewById(R.id.layoutStartWorkout);
        layoutStopPause = (LinearLayout)view.findViewById(R.id.layoutStopPause);
        layoutChooseActivity = (LinearLayout)view.findViewById(R.id.layoutChoosActivity);
        layoutChooseDistance = (LinearLayout)view.findViewById(R.id.layoutChooseDistance);
        layoutChooseSpeed = (LinearLayout)view.findViewById(R.id.layoutChooseSpeed);
        txtChooseActivity = (TextView)view.findViewById(R.id.txtChooseActivity);
        txtChooseDistance = (TextView)view.findViewById(R.id.txtChooseDistanceUnit);
        txtChooseSpeed = (TextView)view.findViewById(R.id.txtChooseSpeedUnit);
        txtDuration = (TextView)view.findViewById(R.id.txtDuration);
        txtTitleSpeed = (TextView)view.findViewById(R.id.txtTitleSpeed);
        txtTitleDistance = (TextView)view.findViewById(R.id.txtTitleDistance);
        txtTitleCalories = (TextView)view.findViewById(R.id.txtTitleCalories);
        txtSpeed = (TextView)view.findViewById(R.id.txtSpeed);
        txtDistance = (TextView)view.findViewById(R.id.txtDistance);
        txtCalories = (TextView)view.findViewById(R.id.txtCalories);
        iconSpeed = (ImageView)view.findViewById(R.id.iconSpeed);
        iconDistance = (ImageView)view.findViewById(R.id.iconDistance);
        iconCalories = (ImageView)view.findViewById(R.id.iconCalories);
        btnStartWorkout = (Button)view.findViewById(R.id.btnStartWorkout);
        btnStop = (Button)view.findViewById(R.id.btnStopWorkout);
        btnPause = (Button)view.findViewById(R.id.btnPauseWorkout);
        btnStartWorkout.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        layoutChooseActivity.setOnClickListener(this);
        layoutChooseDistance.setOnClickListener(this);
        layoutChooseSpeed.setOnClickListener(this);
        myInterface = this;

        // Check saved states
        if(savedInstanceState != null){
            layoutStartWorkout.setVisibility(savedInstanceState.getInt("start_layout")==0?View.GONE:View.VISIBLE);
            layoutInfoWork.setVisibility(savedInstanceState.getInt("info_layout")==0?View.GONE:View.VISIBLE);
            layoutStopPause.setVisibility(savedInstanceState.getInt("stoppause_layout")==0?View.GONE:View.VISIBLE);
            txtChooseActivity.setText(savedInstanceState.getString("choose_activity"));
            txtChooseDistance.setText(savedInstanceState.getString("choose_distance"));
            txtChooseSpeed.setText(savedInstanceState.getString("choose_speed"));
            txtDuration.setText(savedInstanceState.getString("duration_text"));
            txtDistance.setText(savedInstanceState.getString("distance_text"));
            txtSpeed.setText(savedInstanceState.getString("speed_text"));
            txtTitleSpeed.setText(savedInstanceState.getString("title_speed"));
            txtTitleDistance.setText(savedInstanceState.getString("title_distance"));
            isTimerRunning = savedInstanceState.getBoolean("is_running");
            isTimerPaused = savedInstanceState.getBoolean("is_paused");
            previousTime = savedInstanceState.getLong("previous_time");
            previousLocation = savedInstanceState.getParcelable("previous_location");
            timeStart = savedInstanceState.getLong("time_start");
            polyline = savedInstanceState.getParcelable("polyline");
            factorSpeed = savedInstanceState.getDouble("factorSpeed");
            factorDistance = savedInstanceState.getDouble("factorDistance");
            distance = savedInstanceState.getDouble("distance");
            calories = savedInstanceState.getInt("calories");
            typeFactor = savedInstanceState.getDouble("typeFactor");
            avgSpeed = savedInstanceState.getDouble("avgSpeed");
            countAvgSpeed = savedInstanceState.getLong("countAvgSpeed");

            txtCalories.setText(String.valueOf(calories));

            if(isTimerRunning){
                if(timeStart > 0)
                    runTimerDuration();
                btnPause.setText(R.string.pause);
                btnPause.setBackgroundColor(getResources().getColor(R.color.accent_material_light));
            }
            else{
                if(isTimerPaused)
                    timePause = savedInstanceState.getLong("time_pause");
                btnPause.setText(R.string.restart);
                btnPause.setBackgroundColor(getResources().getColor(R.color.primaryDarkColor));
            }
        }

        // Change user interface if landscape orientation
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            iconSpeed.setVisibility(View.GONE);
            iconDistance.setVisibility(View.GONE);
            iconCalories.setVisibility(View.GONE);
            txtTitleSpeed.setTextSize(16);
            txtTitleDistance.setTextSize(16);
            txtTitleCalories.setTextSize(16);
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            rootLayoutHome.setOrientation(LinearLayout.HORIZONTAL);
            layoutInfoWork.requestLayout();
            layoutInfoWork.getLayoutParams().width = displayMetrics.widthPixels/2;
            layoutInfoWork.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("start_layout", (layoutStartWorkout.getVisibility() == View.VISIBLE)?1:0);
        outState.putInt("info_layout", (layoutInfoWork.getVisibility() == View.VISIBLE)?1:0);
        outState.putInt("stoppause_layout", (layoutStopPause.getVisibility() == View.VISIBLE)?1:0);
        outState.putString("choose_activity", txtChooseActivity.getText().toString());
        outState.putString("choose_distance", txtChooseDistance.getText().toString());
        outState.putString("choose_speed", txtChooseSpeed.getText().toString());
        outState.putString("duration_text", txtDuration.getText().toString());
        outState.putString("distance_text", txtDistance.getText().toString());
        outState.putString("speed_text", txtSpeed.getText().toString());
        outState.putString("title_speed", txtTitleSpeed.getText().toString());
        outState.putString("title_distance", txtTitleDistance.getText().toString());
        outState.putBoolean("is_running", isTimerRunning);
        outState.putBoolean("is_paused", isTimerPaused);
        outState.putLong("previous_time", previousTime);
        outState.putParcelable("previous_location", previousLocation);
        outState.putLong("time_start", timeStart);
        outState.putLong("time_pause", timePause);
        outState.putParcelable("polyline", polyline);
        outState.putDouble("factorSpeed", factorSpeed);
        outState.putDouble("factorDistance", factorDistance);
        outState.putDouble("distance",distance);
        outState.putInt("calories",calories);
        outState.putDouble("typeFactor", typeFactor);
        outState.putDouble("avgSpeed", avgSpeed);
        outState.putLong("countAvgSpeed", countAvgSpeed);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnStartWorkout:
                startWorkout();
                break;
            case R.id.btnStopWorkout:
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getString(R.string.want_stop_worktout))
                        .setTitle(context.getString(R.string.stop_workout))
                        .setCancelable(false)
                        .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                stopWorkout();
                            }
                        })
                        .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.btnPauseWorkout:
                pauseWorkout();
                break;
            case R.id.layoutChoosActivity:
                chooseActivityDialog();
                break;
            case R.id.layoutChooseDistance:
                chooseDistanceDialog();
                break;
            case R.id.layoutChooseSpeed:
                chooseSpeedDialog();
                break;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        //
        //updateMapView(latLng); // For Testing
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        initializeGoogleMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == LOCATION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            LatLng latLng = new LatLng(40.416750,-3.703813); // Madrid by default
            LocationManager mLocationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

            try {
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null)
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                }

                updateMapView(latLng);
            }
            catch (SecurityException e){Log.e("MAP_PERMISSIONS", "Error!!: Al inicializar el mapa");}
        }
    }

    /**
     * Initilize map
     */
    private void initializeGoogleMap() {
        try {
            LatLng latLng = new LatLng(40.416750,-3.703813); // Madrid by default

            // Check location permissions
            if (Build.VERSION.SDK_INT >= KITKAT && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permissions, LOCATION_CODE);
            }
            else {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                }
                else{
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(location != null) {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                }
            }

            if(marker != null)
                marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
            if(polyline == null)
                polyline = new PolylineOptions().color(Color.GREEN).width(12).visible(true).zIndex(30);
            else {
                mMap.addPolyline(polyline);
                int index = polyline.getPoints().size() - 1;
                if(index >= 0) {
                    marker.setPosition(polyline.getPoints().get(index));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(polyline.getPoints().get(index), ZOOM));
                }
            }
        }
        catch (SecurityException e){Log.e("MAP_READY", "Error!!: Al inicializar el mapa en onMapReady");}
    }

    /**
     * Update map camera, marker, and polyline
     * @param latLng
     */
    private void updateMapView(LatLng latLng) {
        marker.setPosition(latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
        if(radiusBounds != null){
            if(!radiusBounds.contains(latLng)){
                polyline.add(latLng);
                mMap.addPolyline(polyline);
            }
        }
        else {
            polyline.add(latLng);
            mMap.addPolyline(polyline);
        }
    }

    /**
     * Run async timer
     */
    private void runTimerDuration() {
        if(timePause > 0)
            this.timeStart += (SystemClock.uptimeMillis() - timePause);
        myTimerTask = new MyTimerTask(this.timeStart, txtDuration);
        timer = new Timer();
        timer.schedule(myTimerTask, 0,1000);
        this.isTimerRunning = true;
        this.isTimerPaused = false;
    }

    /**
     * Start workout
     */
    private void startWorkout(){
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            buildAlertMessageNoGps();
            return;
        }

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        MainActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        if(sharedPref.getInt("privacity", 0) == 1) {
            float lat = sharedPref.getFloat("privacitylat", 0);
            float lon = sharedPref.getFloat("privacitylon", 0);
            if(lat != 0 && lon != 0){
                radiusBounds = getRadiusBounds(new LatLng(lat, lon), RADIUS_METERS);
            }
        }
        else
            radiusBounds = null;

        if(txtChooseSpeed.getText().toString().equals(getResources().getString(R.string.km_h_units))) {
            txtTitleSpeed.setText(R.string.speed_km_h);
            this.factorSpeed = 1;
        }
        else {
            txtTitleSpeed.setText(R.string.speed_miles_h);
            this.factorSpeed = KMS_TO_MILES;
        }
        if(txtChooseDistance.getText().toString().equals(getResources().getString(R.string.km_units))) {
            txtTitleDistance.setText(R.string.distance_km);
            this.factorDistance = 1;
        }
        else {
            txtTitleDistance.setText(R.string.distance_miles);
            this.factorDistance = KMS_TO_MILES;
        }

        Resources resources = getResources();
        if(resources.getStringArray(R.array.array_activities)[0].equals(txtChooseActivity.getText().toString())){
            this.typeFactor  = 0.048;
        }
        else if(resources.getStringArray(R.array.array_activities)[1].equals(txtChooseActivity.getText().toString())){
            this.typeFactor  = 0.102;
        }
        else if(resources.getStringArray(R.array.array_activities)[2].equals(txtChooseActivity.getText().toString())){
            this.typeFactor  = 0.082;
        }
        else if(resources.getStringArray(R.array.array_activities)[3].equals(txtChooseActivity.getText().toString())){
            this.typeFactor  = 0.026;
        }
        else if(resources.getStringArray(R.array.array_activities)[4].equals(txtChooseActivity.getText().toString())){
            this.typeFactor  = 0.035;
        }

        this.isTimerPaused = false;
        txtSpeed.setText("0.0");
        txtDistance.setText("0.00");
        txtCalories.setText("0");
        btnPause.setText(R.string.pause);
        btnPause.setBackgroundColor(getResources().getColor(R.color.accent_material_light));
        layoutStartWorkout.setVisibility(View.GONE);
        layoutInfoWork.setVisibility(View.VISIBLE);
        layoutStopPause.setVisibility(View.VISIBLE);

        // Start location updates listener
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, this);
        } catch (SecurityException e){Log.e("MAP_UPDATES", "Error!!: Al iniciar el listener de updates");}

        timePause = 0;
        this.timeStart = SystemClock.uptimeMillis();
        runTimerDuration(); // Start update timer TextView
    }

    /**
     *
     */
    private void pauseWorkout(){
        if(btnPause.getText().toString().equals(getResources().getString(R.string.pause))) {
            // Pause location updates listener
            try {

                locationManager.removeUpdates(this);
            } catch (SecurityException e){Log.e("MAP_UPDATES", "Error!!: Al pausar el listener de updates");}

            if(timer != null){
                timer.cancel();
                timer = null;
                timePause = SystemClock.uptimeMillis();
                this.isTimerRunning = false;
                this.isTimerPaused = true;
            }

            btnPause.setText(R.string.restart);
            btnPause.setBackgroundColor(getResources().getColor(R.color.primaryDarkColor));
        }
        else {
            // Restart location updates listener
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, this);
            } catch (SecurityException e){Log.e("MAP_UPDATES", "Error!!: Al reiniciar el listener de updates");}

            if(timer != null)
                timer.cancel();
            runTimerDuration(); // Restart update timer TextView

            btnPause.setText(R.string.pause);
            btnPause.setBackgroundColor(getResources().getColor(R.color.accent_material_light));
        }
    }

    /**
     *
     */
    private void stopWorkout(){
        // Stop location updates listener
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e){Log.e("MAP_UPDATES", "Error!!: Al quitar el listener de updates");}
        int sportType = -1;
        int distanceUnits = -1;
        int speedUnits = -1;

        Resources resources = getResources();
        String name = "";
        if(resources.getStringArray(R.array.array_activities)[0].equals(txtChooseActivity.getText().toString())){
            sportType = 0;
            name = resources.getStringArray(R.array.array_activities)[0];
        }
        else if(resources.getStringArray(R.array.array_activities)[1].equals(txtChooseActivity.getText().toString())){
            sportType = 1;
            name = resources.getStringArray(R.array.array_activities)[1];
        }
        else if(resources.getStringArray(R.array.array_activities)[2].equals(txtChooseActivity.getText().toString())){
            sportType = 2;
            name = resources.getStringArray(R.array.array_activities)[2];
        }
        else if(resources.getStringArray(R.array.array_activities)[3].equals(txtChooseActivity.getText().toString())){
            sportType = 3;
            name = resources.getStringArray(R.array.array_activities)[3];
        }
        else if(resources.getStringArray(R.array.array_activities)[4].equals(txtChooseActivity.getText().toString())){
            sportType = 4;
            name = resources.getStringArray(R.array.array_activities)[4];
        }

        if(txtChooseSpeed.getText().toString().equals(getResources().getString(R.string.km_h_units)))
            speedUnits = 0;
        else
            speedUnits = 1;

        if(txtChooseDistance.getText().toString().equals(getResources().getString(R.string.km_units)))
            distanceUnits = 0;
        else
            distanceUnits = 1;

        final SportActivityInfo activityInfo = new SportActivityInfo();
        activityInfo.setSportType(sportType);
        activityInfo.setName(name);
        activityInfo.setSpeedUnits(speedUnits);
        activityInfo.setDistanceUnits(distanceUnits);
        activityInfo.setAvgSpeed((this.countAvgSpeed > 0)?(this.avgSpeed/this.countAvgSpeed):0);
        activityInfo.setDistanceKms(this.distance);
        activityInfo.setCalories(this.calories);
        activityInfo.setEncodedPointsList(PolyUtil.encode(polyline.getPoints()));
        if(myTimerTask != null)
            activityInfo.setDurationMillis(myTimerTask.getFinalDuration());
        else{
            this.timeStart += (SystemClock.uptimeMillis() - timePause);
            activityInfo.setDurationMillis(SystemClock.uptimeMillis() - this.timeStart);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.want_save_wokout))
                .setTitle(context.getString(R.string.save_workout_data))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.save_activity), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        UploadSportDataTask uploadData = new UploadSportDataTask(context,myInterface);
                        uploadData.execute(activityInfo);
                        MainActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        if(mMap != null){
                            mMap.clear();
                            polyline.getPoints().clear();
                            initializeGoogleMap();
                        }
                        layoutInfoWork.setVisibility(View.GONE);
                        layoutStopPause.setVisibility(View.GONE);
                        layoutStartWorkout.setVisibility(View.VISIBLE);
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                })
                .setNegativeButton(context.getString(R.string.discard_activity), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        MainActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        if(mMap != null){
                            mMap.clear();
                            polyline.getPoints().clear();
                            initializeGoogleMap();
                        }
                        layoutInfoWork.setVisibility(View.GONE);
                        layoutStopPause.setVisibility(View.GONE);
                        layoutStartWorkout.setVisibility(View.VISIBLE);
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                });
        final AlertDialog alert = builder.create();

        if(polyline.getPoints() != null && polyline.getPoints().size() > 0)
            alert.show();
        else{
            MainActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            if(mMap != null){
                mMap.clear();
                polyline.getPoints().clear();
                initializeGoogleMap();
            }
            layoutInfoWork.setVisibility(View.GONE);
            layoutStopPause.setVisibility(View.GONE);
            layoutStartWorkout.setVisibility(View.VISIBLE);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        if(timer != null){
            timer.cancel();
            timer = null;
            timeStart = 0;
            timePause = 0;
            this.distance = 0;
            this.calories = 0;
            this.avgSpeed = 0;
            this.previousTime = 0;
            this.countAvgSpeed = 0;
            this.isTimerRunning = false;
            this.previousLocation = null;
        }
    }

    /**
     *
     * @param center
     * @param radius
     * @return
     */
    public LatLngBounds getRadiusBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    /**
     * Dialog to choose sports activities
     */
    private void chooseActivityDialog(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.choose_activity_dialog, null);
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.show();

        ListView listViewActivities = (ListView) dialogView.findViewById(android.R.id.list);
        Resources resources = getResources();
        final ArrayList<RowActivity> listActivities = new ArrayList<>();
        listActivities.add(new RowActivity(resources.getDrawable(R.drawable.cycling), resources.getStringArray(R.array.array_activities)[0]));
        listActivities.add(new RowActivity(resources.getDrawable(R.drawable.runningmore), resources.getStringArray(R.array.array_activities)[1]));
        listActivities.add(new RowActivity(resources.getDrawable(R.drawable.running), resources.getStringArray(R.array.array_activities)[2]));
        listActivities.add(new RowActivity(resources.getDrawable(R.drawable.walking), resources.getStringArray(R.array.array_activities)[3]));
        listActivities.add(new RowActivity(resources.getDrawable(R.drawable.walking), resources.getStringArray(R.array.array_activities)[4]));
        ChooseActivityAdapter activityAdapter = new ChooseActivityAdapter(getActivity(), R.layout.row_activities, listActivities);
        listViewActivities.setAdapter(activityAdapter);

        listViewActivities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                txtChooseActivity.setText(listActivities.get(position).getName());
                alertDialog.dismiss();
            }
        });
    }

    /**
     * Dialog to choose distance units
     */
    private void chooseDistanceDialog(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.choose_units_dialog, null);
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.show();

        TextView txtTitle = (TextView)dialogView.findViewById(R.id.txtTitle);
        TextView txtKms = (TextView)dialogView.findViewById(R.id.txtKms);
        TextView txtMiles = (TextView)dialogView.findViewById(R.id.txtMiles);

        txtTitle.setText(R.string.choose_distance);
        txtKms.setText(R.string.km_units);
        txtMiles.setText(R.string.miles_units);

        txtKms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtChooseDistance.setText(R.string.km_units);
                alertDialog.dismiss();
            }
        });
        txtMiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtChooseDistance.setText(R.string.miles_units);
                alertDialog.dismiss();
            }
        });
    }

    /**
     * Dialog to choose speed units
     */
    private void chooseSpeedDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.choose_units_dialog, null);
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.show();

        TextView txtTitle = (TextView) dialogView.findViewById(R.id.txtTitle);
        TextView txtKms = (TextView) dialogView.findViewById(R.id.txtKms);
        TextView txtMiles = (TextView) dialogView.findViewById(R.id.txtMiles);

        txtTitle.setText(R.string.choose_speed);
        txtKms.setText(R.string.km_h_units);
        txtMiles.setText(R.string.miles_h_units);

        txtKms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtChooseSpeed.setText(R.string.km_h_units);
                alertDialog.dismiss();
            }
        });
        txtMiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtChooseSpeed.setText(R.string.miles_h_units);
                alertDialog.dismiss();
            }
        });
    }

    /**
     * Dialog to active gps
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.gps_disabled))
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     *
     * @param kg
     * @param millis
     * @param factor
     * @return
     */
    private int calculateCalories(int kg, long millis, double factor){
        double minutes = (millis % 3600000) / 60000;
        double calories = (kg*2.2)*minutes*factor;
        return (int)calories;
    }

    // LocationListener Methods

    @Override
    public void onLocationChanged(Location location) {
        double distanceSegment = 0;
        if(this.previousLocation != null)
            distanceSegment = location.distanceTo(this.previousLocation);
        this.distance += distanceSegment/1000;
        txtDistance.setText(String.format("%.2f", this.distance * this.factorDistance));

        double timeSegment = 0;
        if(this.previousTime > 0){
            timeSegment = SystemClock.uptimeMillis() - this.previousTime;
        }
        this.previousTime = SystemClock.uptimeMillis();

        double metresPerSeconds = 0;
        if(timeSegment >0){
            metresPerSeconds = distanceSegment / (timeSegment/1000);
        }

        double kmsPerHour = (metresPerSeconds * 18) / 5;
        this.avgSpeed += (kmsPerHour * this.factorSpeed);
        txtSpeed.setText(String.format("%.1f", kmsPerHour * this.factorSpeed));

        String userWeight = MainActivity.USER_ME.getWeight();
        int weight = Integer.parseInt((userWeight != null && !userWeight.equals("") && !userWeight.equals("null")?userWeight:"0"));
        this.calories = calculateCalories(weight,this.myTimerTask.getFinalDuration(),this.typeFactor);
        txtCalories.setText(String.valueOf(this.calories));

        this.countAvgSpeed++;
        this.previousLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateMapView(latLng);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onFinish() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        MainActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}
