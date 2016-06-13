package com.proyecto.enrique.osporthello.Activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
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

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Activity donde el usuario busca a otros usuarios por el nombre. En la lista que se le muestre
 * puede Seguir/Dejar de seguir e iniciar conversación en el Chat.
 */

public class SearchUsersActivity extends AppCompatActivity implements UsersAdapter.FriendsChanges, UserInfoInterface {

    private Context context;
    private LinearLayout layoutSearching;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private ArrayList<User> usersList = null;
    private ArrayList<User> friendsList = null;
    private ArrayList<NameAndImageTask> taskList = null;

    private static UsersAdapter.FriendsChanges myInterface;
    private static UserInfoInterface infoInterface;
    public static boolean FRIENDS_CHANGE = false;

    EditText etxSearchUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        layoutSearching = (LinearLayout)findViewById(R.id.layoutSearching);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        this.context = this;
        this.myInterface = this;
        this.infoInterface = this;
        etxSearchUsers = (EditText)findViewById(R.id.etxSearchUsers);

        // Obtain Recycler
        recycler = (RecyclerView) findViewById(R.id.recyclerViewSearch);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);
        taskList = new ArrayList<>();

        if(savedInstanceState != null){
            etxSearchUsers.setText(savedInstanceState.getString("etxSearch"));
            usersList = (ArrayList<User>) savedInstanceState.getSerializable("userslist");
            friendsList = (ArrayList<User>) savedInstanceState.getSerializable("friendslist");

            if(usersList == null || friendsList == null){
                searchUsersByName();
            }
            else {
                // Instance adapter
                adapter = new UsersAdapter(context, myInterface, infoInterface, usersList, friendsList);
                recycler.setAdapter(adapter);
            }
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
        outState.putString("etxSearch", etxSearchUsers.getText().toString());
    }

    /**
     * Obtains my friends list
     * @return
     */
    private void searchUsersByName() {
        if(taskList != null){
            for (NameAndImageTask task : taskList) {
                if(task != null)
                    task.cancel(true);
            }
            taskList.clear();
        }

        String name = etxSearchUsers.getText().toString();
        name = name.trim();
        if(name.isEmpty())
            return;

        adapter = new UsersAdapter(context, myInterface, infoInterface, new ArrayList<User>(), new ArrayList<User>());
        recycler.setAdapter(adapter);
        layoutSearching.setVisibility(View.VISIBLE);
        User user = MainActivity.USER_ME;
        String city = (user.getCity()==null || user.getCity().equals(""))?"null":user.getCity();
        ApiClient.getUsersByName("api/users/search/" + city + "/" + name + "/" + user.getEmail(), new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                layoutSearching.setVisibility(View.GONE);
                Toast.makeText(context, R.string.connection_error,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                layoutSearching.setVisibility(View.GONE);
                Toast.makeText(context, R.string.connection_error,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                layoutSearching.setVisibility(View.GONE);
                Toast.makeText(context, R.string.connection_error,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        usersList = AnalyzeJSON.analyzeAllUsers(response);
                        friendsList = AnalyzeJSON.analyzeMyFriends(response);
                        // Instance adapter
                        adapter = new UsersAdapter(context, myInterface, infoInterface, usersList, friendsList);
                        recycler.setAdapter(adapter);

                        if(usersList != null){
                            for (int i = 0; i < usersList.size(); i++) {
                                NameAndImageTask mTask = new NameAndImageTask(usersList.get(i).getEmail(), null, null, i, infoInterface);
                                taskList.add(mTask);
                                mTask.execute();
                            }
                        }
                    }
                    layoutSearching.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    layoutSearching.setVisibility(View.GONE);
                    Toast.makeText(context, R.string.connection_error,Toast.LENGTH_SHORT).show();
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
    public void onFriendsChanges(String friendEmail, boolean added) {
        ShowFriendsFragment.FRIENDS_CHANGE = true;
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
      try {
            usersList.get(index).setFirstname(userInfo.getFirstname());
            usersList.get(index).setLastname(userInfo.getLastname());
            usersList.get(index).setImage(userInfo.getImage());
            if (recycler != null && recycler.getAdapter() != null && recycler.getAdapter().getItemCount() > index)
                recycler.getAdapter().notifyItemChanged(index);
        } catch (Exception e){
            Log.e("TASK_IMAGE_ERROR","task index error");
        }
    }
}
