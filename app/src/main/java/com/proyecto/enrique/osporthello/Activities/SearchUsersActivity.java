package com.proyecto.enrique.osporthello.Activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Adapters.UsersAdapter;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Fragments.FriendsFragment;
import com.proyecto.enrique.osporthello.Fragments.ShowFriendsFragment;
import com.proyecto.enrique.osporthello.IndeterminateDialogTask;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SearchUsersActivity extends AppCompatActivity implements UsersAdapter.FriendsChanges {

    private Context context;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private ArrayList<User> usersList = null;
    private ArrayList<User> friendsList = null;

    private static UsersAdapter.FriendsChanges myInterface;
    public static boolean FRIENDS_CHANGE = false;

    EditText etxSearchUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        this.context = this;
        this.myInterface = this;
        etxSearchUsers = (EditText)findViewById(R.id.etxSearchUsers);
        usersList = new ArrayList<>();
        friendsList = new ArrayList<>();

        // Obtain Recycler
        recycler = (RecyclerView) findViewById(R.id.recyclerViewSearch);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        if(savedInstanceState != null){
            usersList = (ArrayList<User>) savedInstanceState.getSerializable("userslist");
            friendsList = (ArrayList<User>) savedInstanceState.getSerializable("friendslist");
            // Instance adapter
            adapter = new UsersAdapter(context, myInterface, usersList, friendsList);
            recycler.setAdapter(adapter);
        }

        // To show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchUsersByName();
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("userslist", usersList);
        outState.putSerializable("friendslist", friendsList);
    }

    /**
     * Obtains list of my friends
     * @return
     */
    private void searchUsersByName() {
        String name = etxSearchUsers.getText().toString();
        if(name.isEmpty())
            return;

        final IndeterminateDialogTask progressDialog = new IndeterminateDialogTask(SearchUsersActivity.this, "Searching...");
        progressDialog.execute();

        User user = MainActivity.USER_ME;
        ApiClient.getUsersByName("api/users/search/" + user.getCity() + "/" + name + "/" + user.getEmail(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressDialog.cancel(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        usersList = AnalyzeJSON.analyzeAllUsers(response);
                        friendsList = AnalyzeJSON.analyzeMyFriends(response);
                        // Instance adapter
                        adapter = new UsersAdapter(context, myInterface, usersList, friendsList);
                        recycler.setAdapter(adapter);
                    }
                    progressDialog.cancel(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.cancel(true);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(FRIENDS_CHANGE == true) {
                    ShowFriendsFragment.FRIENDS_CHANGE = true;
                    setResult(RESULT_OK);
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(FRIENDS_CHANGE == true) {
            ShowFriendsFragment.FRIENDS_CHANGE = true;
            setResult(RESULT_OK);
        }
    }

    @Override
    public void onFriendsChanges() {
        ShowFriendsFragment.FRIENDS_CHANGE = true;
    }
}
