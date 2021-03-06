package com.proyecto.enrique.osporthello.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Adapters.FriendsAdapter;
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

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Activity donde se muestran  la lista de usuarios a los que estás siguiendo, donde el usuario puede
 * iniciar conversaciones en el Chat y Dejar de seguir.
 */

public class FollowingActivity extends AppCompatActivity implements FriendsAdapter.FriendsChanges, UserInfoInterface {

    private ProgressBar progressBar;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;

    private static FriendsAdapter.FriendsChanges myInterface;
    private static UserInfoInterface infoInterface;
    private ArrayList<User> friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        this.myInterface = this;
        this.infoInterface = this;
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
            adapter = new FriendsAdapter(getApplicationContext(), myInterface, infoInterface, friendsList);
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
     * Obtains my friends list
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
                    Toast.makeText(getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (!response.getString("data").equals("null")) {
                            friendsList = AnalyzeJSON.analyzeAllUsers(response);
                            adapter = new FriendsAdapter(getApplicationContext(), myInterface, infoInterface, friendsList);
                            recycler.setAdapter(adapter);

                            if(friendsList != null){
                                for (int i = 0; i < friendsList.size(); i++) {
                                    new NameAndImageTask(friendsList.get(i).getEmail(), null, null, i, infoInterface).execute();
                                }
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), R.string.connection_error,Toast.LENGTH_SHORT).show();
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

    @Override
    public void onInfoUserChanges(User userInfo, int index) {
        if(friendsList == null || friendsList.size() <= index)
            return;
        friendsList.get(index).setFirstname(userInfo.getFirstname());
        friendsList.get(index).setLastname(userInfo.getLastname());
        friendsList.get(index).setImage(userInfo.getImage());
        if(recycler != null && recycler.getAdapter() != null)
            recycler.getAdapter().notifyItemChanged(index);
    }
}
