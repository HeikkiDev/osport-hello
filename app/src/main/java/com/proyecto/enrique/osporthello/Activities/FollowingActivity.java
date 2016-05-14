package com.proyecto.enrique.osporthello.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Fragments.ShowFriendsFragment;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class FollowingActivity extends AppCompatActivity implements FriendsAdapter.FriendsChanges {

    private ProgressBar progressBar;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;

    private static FriendsAdapter.FriendsChanges myInterface;
    private ArrayList<User> friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        this.myInterface = this;
        progressBar = (ProgressBar)findViewById(R.id.progressFollowing);
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
            getMyFriends();
        }
        else{
            friendsList = (ArrayList<User>) savedInstanceState.getSerializable("friendsList");
            if(friendsList == null) {
                progressBar.setVisibility(View.VISIBLE);
                getMyFriends();
                return;
            }
            adapter = new FriendsAdapter(getApplicationContext(), myInterface, friendsList);
            recycler.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("friendsList", friendsList);
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
     * Obtains list of my friends
     * @return
     */
    private void getMyFriends(){
        try {
            User user = MainActivity.USER_ME;

            ApiClient.getMyFriends("api/friends/following" + "/" + user.getEmail(),  new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("FRIENDS", "ERROR!!");
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (!response.getString("data").equals("null")) {
                            friendsList = AnalyzeJSON.analyzeAllUsers(response);
                            adapter = new FriendsAdapter(getApplicationContext(), myInterface, friendsList);
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
        catch (Exception e){
            Log.e("FRIENDS", "ERROR!!");
        }
    }

    @Override
    public void onFriendsChanges() {
        ShowFriendsFragment.FRIENDS_CHANGE = true;
    }
}
