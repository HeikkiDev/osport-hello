package com.proyecto.enrique.osporthello.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Adapters.FriendsAdapter;
import com.proyecto.enrique.osporthello.Adapters.UsersAdapter;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Fragments.ShowFriendsFragment;
import com.proyecto.enrique.osporthello.IndeterminateDialogTask;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class FollowersActivity extends AppCompatActivity implements UsersAdapter.FriendsChanges {

    private Context context;
    private ProgressBar progressBar;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;

    private static UsersAdapter.FriendsChanges myInterface;
    private ArrayList<User> followingList = null;
    private ArrayList<User> followersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        this.context = this;
        this.myInterface = this;
        progressBar = (ProgressBar)findViewById(R.id.progressFollowers);
        progressBar.setVisibility(View.GONE);

        // To show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain Recycler
        recycler = (RecyclerView) findViewById(R.id.recyclerViewFriends);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        if(savedInstanceState == null) {
            progressBar.setVisibility(View.VISIBLE);
            getMyFollowers();
        }
        else{
            followersList = (ArrayList<User>) savedInstanceState.getSerializable("followersList");
            followingList = (ArrayList<User>)savedInstanceState.getSerializable("followingList");
            if(followersList == null)
                return;
            adapter = new UsersAdapter(context, myInterface, followersList, followingList);
            recycler.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("followersList", followersList);
        outState.putSerializable("followingList", followingList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
    }

    /**
     * Obtains list of my followers
     * @return
     */
    private void getMyFollowers() {
        User user = MainActivity.USER_ME;
        ApiClient.getUsersByName("api/friends/followers" + "/" + user.getEmail(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        followersList = AnalyzeJSON.analyzeAllUsers(response);
                        followingList = AnalyzeJSON.analyzeMyFriends(response);
                        // Instance adapter
                        adapter = new UsersAdapter(context, myInterface, followersList, followingList);
                        recycler.setAdapter(adapter);
                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onFriendsChanges() {
        ShowFriendsFragment.FRIENDS_CHANGE = true;
    }
}