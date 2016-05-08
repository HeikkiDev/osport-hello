package com.proyecto.enrique.osporthello.Adapters;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.drive.internal.StringListResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.SportActivityInfo;
import com.proyecto.enrique.osporthello.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by enrique on 8/05/16.
 */
public class MyMapActivitiesAdapter extends RecyclerView.Adapter<MyMapActivitiesAdapter.MapViewHolder>{
    Context context;
    private ArrayList<SportActivityInfo> items;

    // Constructor
    public MyMapActivitiesAdapter(Context context, ArrayList<SportActivityInfo> activities) {
        this.context = context;
        this.items = activities;
    }

    @Override
    public MapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_cardview, parent, false);
        return new MapViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(final MapViewHolder viewHolder, final int i) {
        if(items.get(i).getEmail().equals(MainActivity.USER_ME.getEmail())){
            viewHolder.infoUserLayout.setVisibility(View.GONE);
        }
        else{
            //TODO: Descargar imagen y nombre de este usuario con una tarea asíncrona, poner visible el layout
        }

        long millis = items.get(i).getDurationMillis();
        int hours = (int)millis / 3600000;
        int minutes = (int) (millis % 3600000) / 60000;
        int seconds = (int) ((millis % 3600000) % 60000) / 1000 ;
        viewHolder.duration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        viewHolder.distance.setText(String.format("%.2f",items.get(i).getDistanceMetres()/1000));
        viewHolder.speed.setText(String.format("%.1f",items.get(i).getAvgSpeed()));
        viewHolder.calories.setText(String.valueOf(items.get(i).getCalories()));
        viewHolder.name.setText(items.get(i).getName());

        if(items.get(i).getSpeedUnits().equals(context.getResources().getString(R.string.km_h_units))) {
            viewHolder.titleSpeed.setText(R.string.speed_km_h_oneline);
        }
        else {
            viewHolder.titleSpeed.setText(R.string.speed_miles_h_oneline);
        }
        if(items.get(i).getDistanceUnits().equals(context.getResources().getString(R.string.km_units))) {
            viewHolder.titleDistance.setText(R.string.distance_km_oneline);
        }
        else {
            viewHolder.titleDistance.setText(R.string.distance_miles_oneline);
        }

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: ABRIR ACTIVITY CON DETALLES DEL ENTRENAMIENTO...
            }
        });

        viewHolder.initializeMapView();

        // Get the SportActivityInfo for this item and attach it to the MapView
        SportActivityInfo item = items.get(i);
        viewHolder.mapView.setTag(item);

        // Ensure the map has been initialised by the on map ready callback in ViewHolder.
        // If it is not ready yet, it will be initialised with the NamedLocation set as its tag
        // when the callback is received.
        if (viewHolder.map != null) {
            // The map is already ready to be used
            setMapLocation(viewHolder.map, item);
        }
    }

    /**
     * Calculate map bounds, add markers and polyline
     */
    private void setMapLocation(GoogleMap map, SportActivityInfo data) {
        // Polyline points
        List<LatLng> geoPoints = PolyUtil.decode(data.getEncodedPointsList());

        // Calculate map bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latlng : geoPoints) {
            builder.include(latlng);
        }

        LatLngBounds latLngBounds = builder.build();
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,5));
        map.addPolyline(new PolylineOptions().addAll(geoPoints).color(Color.GREEN).width(12).visible(true).zIndex(30));
        map.addMarker(new MarkerOptions().position(geoPoints.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        map.addMarker(new MarkerOptions().position(geoPoints.get(geoPoints.size()-1)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    class MapViewHolder extends RecyclerView.ViewHolder  implements OnMapReadyCallback {
        GoogleMap map;

        public LinearLayout infoUserLayout;
        public CircleImageView circleImageView;
        public TextView username;
        public CardView cardView;
        public TextView name;
        public MapView mapView;
        public TextView duration;
        public TextView distance;
        public TextView speed;
        public TextView calories;
        public TextView titleDistance;
        public TextView titleSpeed;

        public MapViewHolder(View v) {
            super(v);

            infoUserLayout = (LinearLayout)v.findViewById(R.id.userInfoLayout);
            circleImageView = (CircleImageView)v.findViewById(R.id.user_image);
            username = (TextView)v.findViewById(R.id.username);
            cardView = (CardView)v.findViewById(R.id.activityCardView);
            name = (TextView) v.findViewById(R.id.txtActivityName);
            mapView = (MapView)v.findViewById(R.id.mapActivity);
            duration = (TextView)v.findViewById(R.id.txtDuration);
            distance = (TextView)v.findViewById(R.id.txtDistance);
            speed = (TextView)v.findViewById(R.id.txtSpeed);
            calories = (TextView)v.findViewById(R.id.txtCalories);
            titleDistance = (TextView)v.findViewById(R.id.txtTitleDistance);
            titleSpeed = (TextView)v.findViewById(R.id.txtTitleSpeed);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(context.getApplicationContext());
            map = googleMap;

            SportActivityInfo data = (SportActivityInfo) mapView.getTag();
            if (data != null) {
                setMapLocation(map, data);
            }
        }

        /**
         * Initialises the MapView by calling its lifecycle methods.
         */
        public void initializeMapView() {
            if (mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
        }
    }
}
