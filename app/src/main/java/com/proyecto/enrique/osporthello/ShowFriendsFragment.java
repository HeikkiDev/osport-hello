package com.proyecto.enrique.osporthello;

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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class ShowFriendsFragment extends Fragment {

    private Context context;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    //private ArrayList<User> friendsList = new ArrayList<>();

    public ShowFriendsFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_friends, container, false);

        // Obtain Recycler
        recycler = (RecyclerView) view.findViewById(R.id.recyclerViewFriends);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(lManager);

        getMyFriends();
        return view;
    }

    /**
     * Obtains list of my friends
     * @return
     */
    private void getMyFriends(){
        // Instance adapter
        adapter = new FriendsAdapter(context, FriendsFragment.FRIENDS_LIST);
        recycler.setAdapter(adapter);
    }
}
