package com.proyecto.enrique.osporthello;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ChatFragment extends Fragment {

    private Context context;
    private ProgressBar progressBar;
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
        progressBar = (ProgressBar)view.findViewById(R.id.progressChats);
        progressBar.setVisibility(View.GONE);

        context = getActivity().getApplicationContext();
        listChats = new ArrayList<>();

        // Obtain Recycler
        recycler = (RecyclerView) view.findViewById(R.id.recyclerViewChats);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(lManager);

        if(savedInstanceState == null) {
            progressBar.setVisibility(View.VISIBLE);
            getMyChats();
        }

        return view;
    }

    private void updateFromLocalDB() {
        LocalDataBase dataBase = new LocalDataBase(context);
        Cursor cursor = dataBase.getMyChats(MainActivity.USER_ME.getEmail());

        if (cursor.moveToFirst()) {
            listChats.clear();
            do {
                Chat chat = new Chat();
                chat.setId(cursor.getInt(cursor.getColumnIndex(LocalDataBase.CHAT_ID)));
                chat.setReceiver_email(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_RECEIVER)));
                chat.setReceiver_name(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_NAME)));
                chat.setReceiver_image(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_IMAGE)));
                listChats.add(chat);
            } while (cursor.moveToNext());

            progressBar.setVisibility(View.GONE);
            adapter = new ChatsAdapter(context, listChats);
            recycler.setAdapter(adapter);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            if(savedInstanceState.containsKey("chatsList")) {
                listChats = (ArrayList<Chat>) savedInstanceState.getSerializable("chatsList");
                adapter = new ChatsAdapter(context, listChats);
                recycler.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("chatsList", listChats);
    }

    private void getMyChats() {
        try {
            AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
            client.setTimeout(10000);
            User user = MainActivity.USER_ME;
            client.get(MainActivity.HOST + "api/chats/" + user.getEmail() + "/" + user.getApiKey(), new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("CHATS", "ERROR!!");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (!response.getString("data").equals("null")) {
                            listChats = AnalyzeJSON.analyzeChats(response);
                            LocalDataBase dataBase = new LocalDataBase(context);
                            dataBase.insertChatList(listChats);
                            updateFromLocalDB();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e){
            Log.e("CHATS", "ERROR!!");
        }
    }
}
