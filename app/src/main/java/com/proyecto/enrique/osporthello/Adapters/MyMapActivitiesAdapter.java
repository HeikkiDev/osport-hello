package com.proyecto.enrique.osporthello.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ImageManager;
import com.proyecto.enrique.osporthello.Models.SportActivityInfo;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.NameAndImageTask;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by enrique on 8/05/16.
 */
public class MyMapActivitiesAdapter extends RecyclerView.Adapter<MyMapActivitiesAdapter.MapViewHolder>{
    Context context;
    private ArrayList<SportActivityInfo> items;

    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private OnLoadMoreListener onLoadMoreListener;

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    // Constructor
    public MyMapActivitiesAdapter(Context context, RecyclerView recyclerView, OnLoadMoreListener interf, ArrayList<SportActivityInfo> activities) {
        this.context = context;
        this.items = activities;
        onLoadMoreListener = interf;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();


            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            if(dy > 0) //check for scroll down
                            {
                                visibleItemCount = linearLayoutManager.getChildCount();
                                totalItemCount = linearLayoutManager.getItemCount();
                                pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                                if (loading)
                                {
                                    if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                                    {
                                        loading = false;
                                        onLoadMoreListener.onLoadMore();
                                        loading = true;
                                    }
                                }
                            }
                        }
                    });
        }
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
            new NameAndImageTask(items.get(i).getEmail() ,viewHolder.username, viewHolder.circleImageView).execute();
            viewHolder.infoUserLayout.setVisibility(View.VISIBLE);
        }

        long millis = items.get(i).getDurationMillis();
        int hours = (int)millis / 3600000;
        int minutes = (int) (millis % 3600000) / 60000;
        int seconds = (int) ((millis % 3600000) % 60000) / 1000 ;
        viewHolder.duration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        viewHolder.distance.setText(String.format("%.2f",items.get(i).getDistanceKms()));
        viewHolder.speed.setText(String.format("%.1f",items.get(i).getAvgSpeed()));
        viewHolder.calories.setText(String.valueOf(items.get(i).getCalories()));
        String name = items.get(i).getName();
        viewHolder.name.setText(name.split(";")[0] + context.getString(R.string.name_separator) + name.split(";")[1]);
        try {
            String sportDate = items.get(i).getDate();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date datetime = format.parse(sportDate);
            format = new SimpleDateFormat("dd/MM/yyyy");
            viewHolder.date.setText(format.format(datetime));
        } catch (ParseException e) {e.printStackTrace();}

        if(items.get(i).getSpeedUnits() == 0) {
            viewHolder.titleSpeed.setText(R.string.speed_km_h_oneline);
        }
        else {
            viewHolder.titleSpeed.setText(R.string.speed_miles_h_oneline);
        }
        if(items.get(i).getDistanceUnits() == 0) {
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
        viewHolder.mapView.setClickable(false);

        // Ensure the map has been initialised by the on map ready callback in ViewHolder.
        // If it is not ready yet, it will be initialised with the NamedLocation set as its tag
        // when the callback is received.
        /*if (viewHolder.map != null) {
            // The map is already ready to be used
            setMapLocation(viewHolder.map, item);
        }*/
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
        map.addMarker(new MarkerOptions().position(geoPoints.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(context.getString(R.string.start_route)));
        map.addMarker(new MarkerOptions().position(geoPoints.get(geoPoints.size()-1)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(context.getString(R.string.finish_route))).showInfoWindow();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    class MapViewHolder extends RecyclerView.ViewHolder  implements OnMapReadyCallback {
        GoogleMap map;

        public LinearLayout infoUserLayout;
        public CircleImageView circleImageView;
        public TextView username;
        public CardView cardView;
        public TextView name;
        private TextView date;
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
            date = (TextView)v.findViewById(R.id.txtDate);
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
