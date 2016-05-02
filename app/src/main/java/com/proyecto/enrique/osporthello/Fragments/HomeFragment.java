package com.proyecto.enrique.osporthello.Fragments;

import android.Manifest;
import android.content.Context;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.internal.StringListResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.proyecto.enrique.osporthello.Adapters.ChooseActivityAdapter;
import com.proyecto.enrique.osporthello.Models.RowActivity;
import com.proyecto.enrique.osporthello.MyTimerTask;
import com.proyecto.enrique.osporthello.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by enrique on 16/03/16.
 */
public class HomeFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, LocationListener  {

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
    private Button btnStartWorkout;
    private Button btnStop;
    private Button btnPause;

    private GoogleMap mMap;
    private Marker marker;
    private Context context;
    private AlertDialog alertDialog;
    private Timer timer;
    private MyTimerTask myTimerTask;
    private Location previousLocation;
    private LocationManager locationManager;
    private PolylineOptions polyline;

    private long timeStart = 0;
    private long timePause = 0;
    private double distance = 0d;
    private double factor = 1;
    private long previousTime = 0;
    private boolean isTimerRunning = false;
    private boolean isTimerPaused = false;

    private final int KITKAT = 19;
    private final int LOCATION_CODE = 1;
    private final double KMS_TO_MILES = 0.621371;
    private static int ZOOM = 6;

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
        btnStartWorkout = (Button)view.findViewById(R.id.btnStartWorkout);
        btnStop = (Button)view.findViewById(R.id.btnStopWorkout);
        btnPause = (Button)view.findViewById(R.id.btnPauseWorkout);
        btnStartWorkout.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        layoutChooseActivity.setOnClickListener(this);
        layoutChooseDistance.setOnClickListener(this);
        layoutChooseSpeed.setOnClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Check saved states
        if(savedInstanceState != null){
            layoutStartWorkout.setVisibility(savedInstanceState.getInt("start_layout")==0?View.GONE:View.VISIBLE);
            layoutInfoWork.setVisibility(savedInstanceState.getInt("info_layout")==0?View.GONE:View.VISIBLE);
            layoutStopPause.setVisibility(savedInstanceState.getInt("stoppause_layout")==0?View.GONE:View.VISIBLE);
            txtChooseActivity.setText(savedInstanceState.getString("choose_activity"));
            txtChooseDistance.setText(savedInstanceState.getString("choose_distance"));
            txtChooseSpeed.setText(savedInstanceState.getString("choose_speed"));
            txtDuration.setText(savedInstanceState.getString("duration_text"));
            txtTitleSpeed.setText(savedInstanceState.getString("title_speed"));
            txtTitleDistance.setText(savedInstanceState.getString("title_distance"));
            isTimerRunning = savedInstanceState.getBoolean("is_running");
            isTimerPaused = savedInstanceState.getBoolean("is_paused");
            timeStart = savedInstanceState.getLong("time_start");

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

            // Change user interface if landscape orientation
            if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                txtTitleSpeed.setTextSize(16);
                txtTitleDistance.setTextSize(16);
                txtTitleCalories.setTextSize(16);
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                rootLayoutHome.setOrientation(LinearLayout.HORIZONTAL);
                layoutInfoWork.requestLayout();
                layoutInfoWork.getLayoutParams().width = displayMetrics.widthPixels/2;
                layoutInfoWork.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }

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
        outState.putString("title_speed", txtTitleSpeed.getText().toString());
        outState.putString("title_distance", txtTitleDistance.getText().toString());
        outState.putBoolean("is_running", isTimerRunning);
        outState.putBoolean("is_paused", isTimerPaused);
        outState.putLong("time_start", timeStart);
        outState.putLong("time_pause", timePause);
    }

    @Override
    public void onPause() {
        super.onPause();
        //if(alertDialog != null)
            //alertDialog.dismiss();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnStartWorkout:
                if(txtChooseSpeed.getText().toString().equals(getResources().getString(R.string.km_h_units))) {
                    txtTitleSpeed.setText(R.string.speed_km_h);
                    this.factor = 1;
                }
                else {
                    txtTitleSpeed.setText(R.string.speed_miles_h);
                    this.factor = KMS_TO_MILES;
                }
                if(txtChooseDistance.getText().toString().equals(getResources().getString(R.string.km_units))) {
                    txtTitleDistance.setText(R.string.distance_km);
                    this.factor = 1;
                }
                else {
                    txtTitleDistance.setText(R.string.distance_miles);
                    this.factor = KMS_TO_MILES;
                }
                txtSpeed.setText("0.0");
                txtDistance.setText("0.00");
                txtCalories.setText("0");
                layoutStartWorkout.setVisibility(View.GONE);
                layoutInfoWork.setVisibility(View.VISIBLE);
                layoutStopPause.setVisibility(View.VISIBLE);

                // Start location updates listener
                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 20, this);
                } catch (SecurityException e){Log.e("MAP_UPDATES", "Error!!: Al iniciar el listener de updates");}

                timePause = 0;
                this.timeStart = SystemClock.uptimeMillis();
                runTimerDuration(); // Start update timer TextView
                break;
            case R.id.btnStopWorkout:
                // Stop location updates listener
                try {
                    locationManager.removeUpdates(this);
                } catch (SecurityException e){Log.e("MAP_UPDATES", "Error!!: Al quitar el listener de updates");}

                // TODO: Para el contador, y todo lo demás, previa confirmación con
                // diálogo o algo. Y luego abre una nueva Activity con los datos del entrenamiento.
                if(timer != null){
                    timer.cancel();
                    timer = null;
                    timeStart = 0;
                    timePause = 0;
                    this.previousTime = 0;
                    this.isTimerRunning = false;
                    this.previousLocation = null;
                    if(mMap != null){
                        mMap.clear();
                        initializeGoogleMap();
                    }
                }

                layoutInfoWork.setVisibility(View.GONE);
                layoutStopPause.setVisibility(View.GONE);
                layoutStartWorkout.setVisibility(View.VISIBLE);
                break;
            case R.id.btnPauseWorkout:
                if(btnPause.getText().toString().equals(getResources().getString(R.string.pause))) {
                    // Pause location updates listener
                    try {

                        locationManager.removeUpdates(this);
                    } catch (SecurityException e){Log.e("MAP_UPDATES", "Error!!: Al pausar el listener de updates");}

                    // TODO: Pausa todas las actualizaciones de datos y el guardado de puntos GPS y tal
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
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 20, this);
                    } catch (SecurityException e){Log.e("MAP_UPDATES", "Error!!: Al reiniciar el listener de updates");}

                    // TODO: Reanuda todas las actualizaciones de datos y el guardado de puntos GPS y tal
                    if(timer != null)
                        timer.cancel();
                    runTimerDuration(); // Restart update timer TextView

                    btnPause.setText(R.string.pause);
                    btnPause.setBackgroundColor(getResources().getColor(R.color.accent_material_light));
                }
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
                    ZOOM = 16;
                }

                updateMapView(latLng);
            }
            catch (SecurityException e){Log.e("MAP_PERMISSIONS", "Error!!: Al inicializar el mapa");}
        }
    }

    /**
     *
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
                    ZOOM = 16;
                }
                else{
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(location != null) {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        ZOOM = 16;
                    }
                }
            }

            if(marker != null)
                marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
            polyline = new PolylineOptions().color(Color.GREEN).width(12).visible(true).zIndex(30);
        }
        catch (SecurityException e){Log.e("MAP_READY", "Error!!: Al inicializar el mapa en onMapReady");}
    }

    /**
     *
     * @param latLng
     */
    private void updateMapView(LatLng latLng) {
        if(marker != null)
            marker.remove();
        marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
        polyline.add(latLng);
        mMap.addPolyline(polyline);
    }

    /**
     *
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
        listActivities.add(new RowActivity(resources.getDrawable(R.drawable.marker), resources.getStringArray(R.array.array_activities)[0]));
        listActivities.add(new RowActivity(resources.getDrawable(R.drawable.marker), resources.getStringArray(R.array.array_activities)[1]));
        listActivities.add(new RowActivity(resources.getDrawable(R.drawable.marker), resources.getStringArray(R.array.array_activities)[2]));
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


    // LocationListener Methods

    @Override
    public void onLocationChanged(Location location) {
        double distanceSegment = 0;
        if(this.previousLocation != null)
            distanceSegment = location.distanceTo(this.previousLocation);
        this.distance += distanceSegment/1000;
        txtDistance.setText(String.format("%.2f", this.distance * factor));

        double timeSegment = 0;
        if(this.previousTime > 0){
            timeSegment = SystemClock.uptimeMillis() - this.previousTime;
        }
        this.previousTime = SystemClock.uptimeMillis();

        double metresPerSeconds = 0;
        if(timeSegment >0){
            metresPerSeconds = distanceSegment / timeSegment;
        }

        double kmsPerHour = (metresPerSeconds * 18) / 5;
        txtSpeed.setText(String.format("%.1f", kmsPerHour * factor));

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
}
