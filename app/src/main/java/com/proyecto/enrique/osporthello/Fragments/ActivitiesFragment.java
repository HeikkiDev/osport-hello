package com.proyecto.enrique.osporthello.Fragments;

import android.app.Activity;
import android.content.Context;
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
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Adapters.MyMapActivitiesAdapter;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Models.Chat;
import com.proyecto.enrique.osporthello.Models.SportActivityInfo;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by enrique on 16/03/16.
 */
public class ActivitiesFragment extends Fragment implements MyMapActivitiesAdapter.OnLoadMoreListener{

    private Context context;
    private TextView txtNotToShow;
    private ProgressBar progressBar;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int currentPage = 0, totalPages = 0;
    private ArrayList<SportActivityInfo> listActivites;
    private MyMapActivitiesAdapter.OnLoadMoreListener myInterface;

    public ActivitiesFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.updateList);
        txtNotToShow = (TextView)view.findViewById(R.id.txtNotToShow);
        progressBar = (ProgressBar)view.findViewById(R.id.progressActivities);
        txtNotToShow.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        context = getContext();
        myInterface = this;

        // Obtain Recycler
        recycler = (RecyclerView) view.findViewById(R.id.recyclerViewActivities);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(lManager);

        if(savedInstanceState == null) {
            progressBar.setVisibility(View.VISIBLE);
            getMyActivities();
        }
        else{
            currentPage = savedInstanceState.getInt("currentPage");
            totalPages = savedInstanceState.getInt("totalPages");
            listActivites = (ArrayList<SportActivityInfo>) savedInstanceState.getSerializable("activitiesList");

            if(listActivites == null){
                progressBar.setVisibility(View.VISIBLE);
                getMyActivities();
            }
            else {
                adapter = new MyMapActivitiesAdapter(getActivity().getApplicationContext(), recycler, myInterface, null, listActivites, true);
                recycler.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
                if (listActivites == null || listActivites.isEmpty())
                    txtNotToShow.setVisibility(View.VISIBLE);
            }
        }

        swipeRefreshLayout.setColorSchemeResources(R.color.accentColor, R.color.primaryColor);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyActivities();
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentPage", currentPage);
        outState.putInt("totalPages", totalPages);
        outState.putSerializable("activitiesList", listActivites);
    }

    /**
     * Obtains my activities list
     * @return
     */
    private void getMyActivities(){
        try {
            User user = MainActivity.USER_ME;

            // Page 0 first time
            ApiClient.getMyActivities("api/activity/" + user.getEmail() + "/0",  new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("ACTIVITIES", "ERROR!!");
                    progressBar.setVisibility(View.GONE);
                    txtNotToShow.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    progressBar.setVisibility(View.GONE);
                    txtNotToShow.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    progressBar.setVisibility(View.GONE);
                    txtNotToShow.setVisibility(View.VISIBLE);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (!response.getString("data").equals("null")) {
                            totalPages = response.getJSONObject("data_aux").getInt("TotalPages");
                            listActivites = AnalyzeJSON.analyzeListActivities(response);
                            // Instance adapter
                            adapter = new MyMapActivitiesAdapter(context.getApplicationContext(), recycler, myInterface, null, listActivites, true);
                            recycler.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                            txtNotToShow.setVisibility(View.GONE);
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            txtNotToShow.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                        txtNotToShow.setVisibility(View.VISIBLE);
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
            Log.e("ACTIVITIES", "ERROR!!");
        }
    }

    /**
     * Obtains more activities
     * @return
     */
    private void getMoreActivities(){
        try {
            User user = MainActivity.USER_ME;

            // Page 0 first time
            ApiClient.getMyActivities("api/activity/" + user.getEmail() + "/" + currentPage,  new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("ACTIVITIES", "ERROR!!");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Log.e("ACTIVITIES", "ERROR!!");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.e("ACTIVITIES", "ERROR!!");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (!response.getString("data").equals("null")) {
                            int itemsCount = listActivites.size();
                            totalPages = response.getJSONObject("data_aux").getInt("TotalPages");
                            ArrayList<SportActivityInfo> moreActivities = AnalyzeJSON.analyzeListActivities(response);
                            listActivites.addAll(moreActivities);
                            adapter.notifyDataSetChanged();
                            recycler.setAdapter(adapter);
                            recycler.scrollToPosition(itemsCount-1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e){
            Log.e("ACTIVITIES", "ERROR!!");
        }
    }

    @Override
    public void onLoadMore() {
        if(totalPages > 1 && currentPage < totalPages-1) {
            currentPage += 1;
            getMoreActivities();
        }
    }
}
