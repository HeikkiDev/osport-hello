package com.proyecto.enrique.osporthello.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Adapters.UsersAdapter;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.AsyncTask.NameAndImageTask;
import com.proyecto.enrique.osporthello.Fragments.ShowFriendsFragment;
import com.proyecto.enrique.osporthello.Interfaces.UserInfoInterface;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class FollowersActivity extends AppCompatActivity implements UsersAdapter.FriendsChanges, UserInfoInterface {

    private Context context;
    private ProgressBar progressBar;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static UsersAdapter.FriendsChanges myInterface;
    private static UserInfoInterface infoInterface;
    private ArrayList<User> followingList = null;
    private ArrayList<User> followersList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.updateList);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        this.context = this;
        this.myInterface = this;
        this.infoInterface = this;
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
            if(followersList == null || followingList == null){
                progressBar.setVisibility(View.VISIBLE);
                getMyFollowers();
                return;
            }
            adapter = new UsersAdapter(context, myInterface, infoInterface, followersList, followingList);
            recycler.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
        }

        swipeRefreshLayout.setColorSchemeResources(R.color.accentColor, R.color.primaryColor);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyFollowers();
            }
        });
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
                Toast.makeText(context, R.string.connection_error,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, R.string.connection_error,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, R.string.connection_error,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        followersList = AnalyzeJSON.analyzeAllUsers(response);
                        followingList = AnalyzeJSON.analyzeMyFriends(response);
                        // Instance adapter
                        adapter = new UsersAdapter(context, myInterface, infoInterface, followersList, followingList);
                        recycler.setAdapter(adapter);

                        if(followersList != null){
                            for (int i = 0; i < followersList.size(); i++) {
                                new NameAndImageTask(followersList.get(i).getEmail(), null, null, i, infoInterface).execute();
                            }
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, R.string.connection_error,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                try {
                    swipeRefreshLayout.setRefreshing(false);
                } catch (Exception e) {
                    //
                }
            }
        });
    }

    @Override
    public void onFriendsChanges(String friendEmail, boolean added) {
        ShowFriendsFragment.FRIENDS_CHANGE = true;
        if(added){
            followingList.add(new User(friendEmail, null, null, null, null));
        }
        else {
            for (int i = 0; i < followingList.size(); i++) {
                User user = followingList.get(i);
                if(user.getEmail().equals(friendEmail))
                    followingList.remove(i);
            }
        }
    }

    @Override
    public void onInfoUserChanges(User userInfo, int index) {
        followersList.get(index).setFirstname(userInfo.getFirstname());
        followersList.get(index).setLastname(userInfo.getLastname());
        followersList.get(index).setImage(userInfo.getImage());
        if(recycler != null && recycler.getAdapter() != null)
            recycler.getAdapter().notifyItemChanged(index);
    }
}
