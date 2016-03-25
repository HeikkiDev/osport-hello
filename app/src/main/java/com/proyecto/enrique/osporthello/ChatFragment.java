package com.proyecto.enrique.osporthello;

import android.content.Context;
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

public class ChatFragment extends Fragment {

    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;

    private ArrayList<Chat> listChats;

    public ChatFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        listChats = new ArrayList<>();

        // Obtain Recycler
        recycler = (RecyclerView) view.findViewById(R.id.recyclerViewChats);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(lManager);

        getMyChats();

        return view;
    }

    private void getMyChats() {
        final IndeterminateDialogTask dialog = new IndeterminateDialogTask(getActivity(), "Loading...");
        try {
            dialog.execute();

            AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
            client.setTimeout(10000);
            User user = MainActivity.USER_ME;
            client.get(MainActivity.HOST + "api/chats/" + user.getEmail() + "/" + user.getApiKey(), new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("CHATS", "ERROR!!");
                    dialog.cancel(true);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (!response.getString("data").equals("null")) {
                            listChats = AnalyzeJSON.analyzeChats(response);
                            adapter = new ChatsAdapter(listChats);
                            recycler.setAdapter(adapter);
                        }
                        dialog.cancel(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.cancel(true);
                    }
                }
            });
        }
        catch (Exception e){
            dialog.cancel(true);
        }
    }
}
