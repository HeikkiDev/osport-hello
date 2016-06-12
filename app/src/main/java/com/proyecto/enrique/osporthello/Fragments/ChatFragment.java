package com.proyecto.enrique.osporthello.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Adapters.ChatsAdapter;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.LocalDataBase;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.Chat;
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
 * Descripción: Fragment que muestra la lista de Chats que tenemos abiertos con otros usuarios.
 */

public class ChatFragment extends Fragment {

    private Context context;
    private TextView txtNotToShow;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<Chat> listChats;

    public ChatFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.updateList);
        txtNotToShow = (TextView)view.findViewById(R.id.txtNotToShow);
        txtNotToShow.setVisibility(View.GONE);

        context = getActivity().getApplicationContext();

        // Obtain Recycler
        recycler = (RecyclerView) view.findViewById(R.id.recyclerViewChats);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(lManager);

        if(savedInstanceState != null) {
            listChats = (ArrayList<Chat>) savedInstanceState.getSerializable("chatsList");
            if(listChats == null){
                getMyChats();
            }
            else {
                adapter = new ChatsAdapter(context, listChats);
                recycler.setAdapter(adapter);
                if(listChats.isEmpty())
                    txtNotToShow.setVisibility(View.VISIBLE);
                else
                    txtNotToShow.setVisibility(View.GONE);
            }
        }
        else{
            updateFromLocalDB();
            getMyChats();
        }

        swipeRefreshLayout.setColorSchemeResources(R.color.accentColor, R.color.primaryColor);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyChats();
            }
        });
        return view;
    }

    private void updateFromLocalDB() {
        LocalDataBase dataBase = new LocalDataBase(context);
        Cursor cursor = dataBase.getMyChats(MainActivity.USER_ME.getEmail());

        if(listChats != null)
            listChats.clear();
        else
            listChats = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Chat chat = new Chat();
                chat.setId(cursor.getInt(cursor.getColumnIndex(LocalDataBase.CHAT_ID)));
                chat.setReceiver_email(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_RECEIVER)));
                chat.setReceiver_name(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_NAME)));
                chat.setReceiver_image(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_IMAGE)));
                listChats.add(chat);
            } while (cursor.moveToNext());
        }
        dataBase.Close();

        adapter = new ChatsAdapter(context, listChats);
        recycler.setAdapter(adapter);

        if(listChats.isEmpty())
            txtNotToShow.setVisibility(View.VISIBLE);
        else
            txtNotToShow.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("chatsList", listChats);
    }

    private void getMyChats() {
        try {
            final User user = MainActivity.USER_ME;
            ApiClient.getUserChats("api/chats/" + user.getEmail() + "/" + user.getApiKey(), new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("CHATS", "ERROR!!");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Log.e("CHATS", "ERROR!!");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.e("CHATS", "ERROR!!");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        LocalDataBase dataBase = new LocalDataBase(context);
                        if (!response.getString("data").equals("null")) {
                            listChats = AnalyzeJSON.analyzeChats(response);
                            dataBase.insertChatList(listChats, user.getEmail());
                        } else {
                            if(listChats != null)
                                listChats.clear();
                            else
                                listChats = new ArrayList<Chat>();
                            dataBase.insertChatList(listChats, user.getEmail());
                        }
                        dataBase.Close();

                        updateFromLocalDB();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    try {
                        swipeRefreshLayout.setRefreshing(false);
                    } catch (Exception e) {

                    }
                }
            });
        }
        catch (Exception e){
            Log.e("CHATS", "ERROR!!");
        }
    }
}
