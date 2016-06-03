package com.proyecto.enrique.osporthello.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Activities.ConfigurationActivity;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Adapters.GeoSearchAdapter;
import com.proyecto.enrique.osporthello.Adapters.UsersAdapter;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.AsyncTask.NameAndImageTask;
import com.proyecto.enrique.osporthello.Interfaces.UserInfoInterface;
import com.proyecto.enrique.osporthello.Models.GeoSearch;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class GeoSearchFragment extends Fragment implements UserInfoInterface, GeoSearchAdapter.FriendsChanges{

    private Context context;
    private ProgressBar progressBar;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RelativeLayout layoutNoGeoSearch;
    private RelativeLayout layoutGeoSearch;
    private TextView txtNotToShow;
    private Spinner spinnerUnits;
    private Spinner spinnerSport;
    private Button btnSearch;
    private Button btnGoConfiguration;

    private static GeoSearchAdapter.FriendsChanges friendInterface;
    private static UserInfoInterface infoInterface;
    private boolean isDownloading = false;
    private ArrayList<GeoSearch> usersList = null;
    private ArrayList<User> friendsList = null;
    private static final String PREFERENCES_FILE = "osporthello_settings";

    public GeoSearchFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_geo_search, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.updateList);
        progressBar = (ProgressBar)view.findViewById(R.id.progressGeoSearch);
        spinnerUnits = (Spinner)view.findViewById(R.id.spinnerUnits);
        spinnerSport = (Spinner)view.findViewById(R.id.spinnerSport);
        btnSearch = (Button)view.findViewById(R.id.btnSearch);
        btnGoConfiguration = (Button)view.findViewById(R.id.btnGoConfguration);
        layoutNoGeoSearch = (RelativeLayout)view.findViewById(R.id.layoutNoGeosearhc);
        layoutGeoSearch = (RelativeLayout)view.findViewById(R.id.layoutGeoSearch);
        txtNotToShow = (TextView)view.findViewById(R.id.txtNotToShowGeo);
        context = getContext();
        friendInterface = this;
        infoInterface = this;

        // Obtain Recycler
        recycler = (RecyclerView)view.findViewById(R.id.recyclerGeoSearch);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(lManager);

        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        if(sharedPref.getInt("geosearch", 0) == 0){
            btnGoConfiguration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ConfigurationActivity.class);
                    startActivity(intent);
                }
            });
            layoutNoGeoSearch.setVisibility(View.VISIBLE);
            layoutGeoSearch.setVisibility(View.GONE);
            btnSearch.setEnabled(false);
        }

        initializeSpinners();
        if(savedInstanceState != null) {
            isDownloading = savedInstanceState.getBoolean("isDownloading");
            spinnerUnits.setSelection(savedInstanceState.getInt("spinnerUnits"));
            spinnerSport.setSelection(savedInstanceState.getInt("spinnerSport"));
            usersList = (ArrayList<GeoSearch>) savedInstanceState.getSerializable("geosearchList");
            friendsList = (ArrayList<User>) savedInstanceState.getSerializable("friendslist");
            if(usersList == null)
                usersList = new ArrayList<>();
            if(friendsList == null)
                friendsList = new ArrayList<>();
            if(isDownloading){
                getGeoSearch();
            }
            else {
                adapter = new GeoSearchAdapter(context, friendInterface, infoInterface, spinnerUnits.getSelectedItemPosition(), usersList, friendsList);
                recycler.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            }
        }

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGeoSearch();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.accentColor, R.color.primaryColor);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getGeoSearch();
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDownloading", isDownloading);
        outState.putSerializable("geosearchList", usersList);
        outState.putSerializable("friendslist", friendsList);
        outState.putInt("spinnerUnits", spinnerUnits.getSelectedItemPosition());
        outState.putInt("spinnerSport", spinnerSport.getSelectedItemPosition());
    }

    private void initializeSpinners() {
        ArrayList<String> listUnits = new ArrayList<String>();
        listUnits.add(getString(R.string.km_units));
        listUnits.add(getString(R.string.miles_units));

        ArrayAdapter<String> adapterU = new ArrayAdapter<String>(context, R.layout.spinner_item,listUnits);
        spinnerUnits.setAdapter(adapterU);

        ArrayList<String> listSport = new ArrayList<String>();
        listSport.add(getString(R.string.cyclist));
        listSport.add(getString(R.string.runner));
        listSport.add(getString(R.string.others));

        ArrayAdapter<String> adapterS = new ArrayAdapter<String>(context, R.layout.spinner_item,listSport);
        spinnerSport.setAdapter(adapterS);
    }

    private void getGeoSearch() {
        isDownloading = true;
        User user = MainActivity.USER_ME;
        int units = spinnerUnits.getSelectedItemPosition();
        int sport = spinnerSport.getSelectedItemPosition();

        progressBar.setVisibility(View.VISIBLE);
        txtNotToShow.setVisibility(View.GONE);
        usersList = new ArrayList<>();
        friendsList = new ArrayList<>();
        adapter = new GeoSearchAdapter(context, friendInterface, infoInterface, spinnerUnits.getSelectedItemPosition(), usersList, friendsList);
        recycler.setAdapter(adapter);

        ApiClient.getGeoSearch("api/users/geosearch/" + user.getEmail() +"/"+ units +"/"+sport, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        usersList = AnalyzeJSON.analyzeGeoSearchUsers(response);
                        friendsList = AnalyzeJSON.analyzeMyFriends(response);
                        // Instance adapter
                        adapter = new GeoSearchAdapter(context, friendInterface, infoInterface, spinnerUnits.getSelectedItemPosition(), usersList, friendsList);
                        recycler.setAdapter(adapter);

                        if(usersList != null){
                            for (int i = 0; i < usersList.size(); i++) {
                                new NameAndImageTask(usersList.get(i).getEmail(), null, null, i, infoInterface).execute();
                            }
                        }
                    }
                    if(usersList != null && usersList.isEmpty())
                        txtNotToShow.setVisibility(View.VISIBLE);
                    else
                        txtNotToShow.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                try {
                    isDownloading = false;
                    swipeRefreshLayout.setRefreshing(false);
                } catch (Exception e) {
                    //
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        if(sharedPref.getInt("geosearch", 0) == 0){
            btnGoConfiguration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ConfigurationActivity.class);
                    startActivity(intent);
                }
            });
            layoutNoGeoSearch.setVisibility(View.VISIBLE);
            layoutGeoSearch.setVisibility(View.GONE);
            btnSearch.setEnabled(false);
        }
        else{
            layoutNoGeoSearch.setVisibility(View.GONE);
            layoutGeoSearch.setVisibility(View.VISIBLE);
            btnSearch.setEnabled(true);
        }
    }

    @Override
    public void onFriendsChanges(String friendEmail, boolean added) {
        if(added){
            friendsList.add(new User(friendEmail, null, null, null, null));
        }
        else {
            for (int i = 0; i < friendsList.size(); i++) {
                User user = friendsList.get(i);
                if(user.getEmail().equals(friendEmail))
                    friendsList.remove(i);
            }
        }
    }

    @Override
    public void onInfoUserChanges(User userInfo, int index) {
        usersList.get(index).setFirstname(userInfo.getFirstname());
        usersList.get(index).setLastname(userInfo.getLastname());
        usersList.get(index).setImage(userInfo.getImage());
        if(recycler != null && recycler.getAdapter() != null)
            recycler.getAdapter().notifyItemChanged(index);
    }
}
