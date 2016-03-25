package com.proyecto.enrique.osporthello;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SearchUsersActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private ArrayList<User> usersList = null;
    private ArrayList<User> friendsList = null;

    public static boolean FRIENDS_CHANGE = false;

    EditText etxSearchUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        etxSearchUsers = (EditText)findViewById(R.id.etxSearchUsers);
        usersList = new ArrayList<>();
        friendsList = FriendsFragment.FRIENDS_LIST;

        // Obtain Recycler
        recycler = (RecyclerView) findViewById(R.id.recyclerViewSearch);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        // To show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchUsersByName();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("userslist", usersList);
        outState.putSerializable("friendslist", friendsList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        usersList = (ArrayList<User>) savedInstanceState.getSerializable("userslist");
        friendsList = (ArrayList<User>) savedInstanceState.getSerializable("friendslist");
        // Instance adapter
        adapter = new UsersAdapter(usersList, friendsList);
        recycler.setAdapter(adapter);
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

        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(10000);
        User user = MainActivity.USER_ME;
        client.get(MainActivity.HOST + "api/users/search/" + user.getCity() + "/" + name + "/" + user.getApiKey(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressDialog.cancel(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        usersList = AnalyzeJSON.analyzeAllUsers(response);
                        // Instance adapter
                        adapter = new UsersAdapter(usersList, friendsList);
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
                if(FRIENDS_CHANGE == true)
                    setResult(RESULT_OK);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(FRIENDS_CHANGE == true)
            setResult(RESULT_OK);
    }
}
