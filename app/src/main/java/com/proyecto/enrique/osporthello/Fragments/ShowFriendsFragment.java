package com.proyecto.enrique.osporthello.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Activities.FollowersActivity;
import com.proyecto.enrique.osporthello.Activities.FollowingActivity;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Adapters.FriendsAdapter;
import com.proyecto.enrique.osporthello.Adapters.UsersAdapter;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class ShowFriendsFragment extends Fragment {

    private Context context;
    private ProgressBar progressBar;
    private TextView txtNumFollowers;
    private TextView txtNumFollowing;
    private LinearLayout layoutFollowers;
    private LinearLayout layoutFollowing;
    private LinearLayout layoutOrientation;

    private static int numFollowers = -1;
    private static int numFollowing = -1;
    public static boolean FRIENDS_CHANGE = false;

    private final int FOLLOWERS = 1;
    private final int FOLLOWING = 2;

    public ShowFriendsFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_friends, container, false);
        txtNumFollowers = (TextView)view.findViewById(R.id.txtNumFollowers);
        txtNumFollowing = (TextView)view.findViewById(R.id.txtNumFollowing);
        layoutFollowers = (LinearLayout)view.findViewById(R.id.layoutFollowers);
        layoutFollowing = (LinearLayout)view.findViewById(R.id.layoutFollowing);
        layoutOrientation = (LinearLayout)view.findViewById(R.id.layoutOrientation);
        progressBar = (ProgressBar)view.findViewById(R.id.progressShowFriends);
        progressBar.setVisibility(View.GONE);
        context = getContext();

        if(savedInstanceState != null){
            numFollowers = savedInstanceState.getInt("numFollowers");
            numFollowing = savedInstanceState.getInt("numFollowing");

            if(numFollowers == -1 || numFollowing == -1){
                progressBar.setVisibility(View.VISIBLE);
                updateFollowCounters();
            }
            else {
                txtNumFollowers.setText(numFollowers + "");
                txtNumFollowing.setText(numFollowing + "");
                progressBar.setVisibility(View.GONE);
            }
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            updateFollowCounters();
        }

        // Change user interface if landscape orientation
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            layoutOrientation.setOrientation(LinearLayout.HORIZONTAL);
        }

        layoutFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                startActivityForResult(intent, FOLLOWERS);
            }
        });

        layoutFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowingActivity.class);
                startActivityForResult(intent, FOLLOWING);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(FRIENDS_CHANGE == true)
            updateFollowCounters();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("numFollowers", numFollowers);
        outState.putInt("numFollowing", numFollowing);
    }

    /**
     * Update followers/following counter from server
     */
    private void updateFollowCounters(){
        try {
            User user = MainActivity.USER_ME;

            ApiClient.getMyFriends("api/friends/follows_counter" + "/" + user.getEmail(),  new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("FOLLWING", "ERROR!!");
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String s,Throwable throwable) {
                    Log.e("FOLLWING", "ERROR!!");
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (!response.getString("data").equals("null")) {
                            numFollowing = response.getJSONArray("data").getJSONObject(0).getInt("Counter");
                            numFollowers = response.getJSONArray("data").getJSONObject(1).getInt("Counter");
                            txtNumFollowers.setText(numFollowers+"");
                            txtNumFollowing.setText(numFollowing+"");
                            progressBar.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e){
            Log.e("FOLLWING", "ERROR!!");
        }
    }

}
