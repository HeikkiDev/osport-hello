package com.proyecto.enrique.osporthello.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.proyecto.enrique.osporthello.R;

/**
 * Created by enrique on 16/03/16.
 */
public class HomeFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private LinearLayout rootLayoutHome;
    private LinearLayout layoutInfoWork;
    private LinearLayout layoutStartWorkout;
    private LinearLayout layoutStopPause;
    private Button btnStartWorkout;
    private Button btnStop;
    private Button btnPause;

    private GoogleMap mMap;
    private Marker marker;
    private Context context;

    private final int KITKAT = 19;
    private final int LOCATION_CODE = 1;
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
        btnStartWorkout = (Button)view.findViewById(R.id.btnStartWorkout);
        btnStop = (Button)view.findViewById(R.id.btnStopWorkout);
        btnPause = (Button)view.findViewById(R.id.btnPauseWorkout);
        btnStartWorkout.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPause.setOnClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Check saved states
        if(savedInstanceState != null){
            layoutStartWorkout.setVisibility(savedInstanceState.getInt("start_layout")==0?View.GONE:View.VISIBLE);
            layoutInfoWork.setVisibility(savedInstanceState.getInt("info_layout")==0?View.GONE:View.VISIBLE);
            layoutStopPause.setVisibility(savedInstanceState.getInt("stoppause_layout")==0?View.GONE:View.VISIBLE);

            // Change user interface if landscape orientation
            if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
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
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnStartWorkout){  // Start
            layoutStartWorkout.setVisibility(View.GONE);
            layoutInfoWork.setVisibility(View.VISIBLE);
            layoutStopPause.setVisibility(View.VISIBLE);
        }
        else if(v.getId() == R.id.btnStopWorkout){  // Stop

            // TODO: Para el contador, y todo lo demás, previa confirmación con
            // diálogo o algo. Y luego abre una nueva Activity con los datos del entrenamiento.

            layoutInfoWork.setVisibility(View.GONE);
            layoutStopPause.setVisibility(View.GONE);
            layoutStartWorkout.setVisibility(View.VISIBLE);
        }
        else if(v.getId() == R.id.btnPauseWorkout){ // Pause

            if(btnPause.getText().toString().equals(getResources().getString(R.string.pause))) {
                // TODO: Pausa todas las actualizaciones de datos y el guardado de puntos GPS y tal
                btnPause.setText(R.string.restart);
                btnPause.setBackgroundColor(getResources().getColor(R.color.primaryDarkColor));
            }
            else {
                // TODO: Reanuda todas las actualizaciones de datos y el guardado de puntos GPS y tal
                btnPause.setText(R.string.pause);
                btnPause.setBackgroundColor(getResources().getColor(R.color.accent_material_light));
            }
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

        LocationManager mLocationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

        try {
            LatLng latLng = new LatLng(40.4381311,-8.1619696); // Madrid by default

            // Check location permissions
            if (Build.VERSION.SDK_INT >= KITKAT && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                    requestPermissions(permissions, LOCATION_CODE);
            }
            else {
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null)
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    ZOOM = 16;
                }
            }

            if(marker != null)
                marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
        }
        catch (SecurityException e){Toast.makeText(context,"Error al obtener posición!\n"+e.getMessage(),Toast.LENGTH_SHORT).show();}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == LOCATION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            LatLng latLng = new LatLng(40.4167051,-3.7056617); // Madrid by default
            LocationManager mLocationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

            try {
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null)
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    ZOOM = 16;
                }

                if(marker != null)
                    marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
            }
            catch (SecurityException e){Toast.makeText(context,"Error al obtener posición!\n"+e.getMessage(),Toast.LENGTH_SHORT).show();}
        }
    }
}
