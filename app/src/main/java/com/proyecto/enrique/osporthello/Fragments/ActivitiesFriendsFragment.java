package com.proyecto.enrique.osporthello.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Adapters.MyMapActivitiesAdapter;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Models.SportActivityInfo;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ActivitiesFriendsFragment extends Fragment {

    private TextView txtNotToShow;
    private ProgressBar progressBar;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;

    private int totalPages = 0;
    private ArrayList<SportActivityInfo> activitiesList = new ArrayList<>();

    public ActivitiesFriendsFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities_friends, container, false);
        txtNotToShow = (TextView)view.findViewById(R.id.txtNotToShow);
        progressBar = (ProgressBar)view.findViewById(R.id.progressFriendsActivities);
        txtNotToShow.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        // Obtain Recycler
        recycler = (RecyclerView) view.findViewById(R.id.recyclerViewFriendsActivities);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(lManager);

        if(savedInstanceState == null) {
            progressBar.setVisibility(View.VISIBLE);
            getFriendsActivities();
        }
        else{
            totalPages = savedInstanceState.getInt("totalPages");
            activitiesList = (ArrayList<SportActivityInfo>) savedInstanceState.getSerializable("activitiesList");
            adapter = new MyMapActivitiesAdapter(getActivity().getApplicationContext(), activitiesList);
            recycler.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
            if(activitiesList.isEmpty())
                txtNotToShow.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("totalPages", totalPages);
        outState.putSerializable("activitiesList", activitiesList);
    }

    /**
     * Obtains my activities list
     * @return
     */
    private void getFriendsActivities(){
        try {
            User user = MainActivity.USER_ME;

            // Page 0 first time
            ApiClient.getFriendsActivities("api/activity/friends/" + user.getEmail() + "/0",  new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("ACTIVITIES", "ERROR!!");
                    progressBar.setVisibility(View.GONE);
                    txtNotToShow.setVisibility(View.VISIBLE);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (!response.getString("data").equals("null")) {
                            totalPages = response.getJSONArray("data").getJSONObject(0).getInt("TotalPages");
                            activitiesList = AnalyzeJSON.analyzeListActivities(response);
                            // Instance adapter
                            adapter = new MyMapActivitiesAdapter(getActivity().getApplicationContext(), activitiesList);
                            recycler.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                            txtNotToShow.setVisibility(View.GONE);
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            txtNotToShow.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e){
            Log.e("ACTIVITIES", "ERROR!!");
        }
    }
}
