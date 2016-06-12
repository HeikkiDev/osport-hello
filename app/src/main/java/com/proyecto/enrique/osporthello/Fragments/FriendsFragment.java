package com.proyecto.enrique.osporthello.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Adapters.MyFragmentPagerAdapter;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;
import com.proyecto.enrique.osporthello.Activities.SearchUsersActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Fragment que contien dos Tabs en los que se muestran los Fragment de ActivitiesFriends y ShowFriends.
 */

public class FriendsFragment extends Fragment {

    ViewPager viewPager;
    TabLayout tabLayout;
    MyFragmentPagerAdapter pagerAdapter;

    private static int SEARCH_CODE = 1;

    public FriendsFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        setHasOptionsMenu(true);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        tabLayout = (TabLayout) view.findViewById(R.id.appbartabs);

        if(pagerAdapter == null)
            pagerAdapter = new MyFragmentPagerAdapter(getChildFragmentManager(), getString(R.string.activities_item), getString(R.string.friends_item));
        viewPager.setAdapter(pagerAdapter);

        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.toolbar_search) {
            Intent intent = new Intent(getContext(), SearchUsersActivity.class);
            startActivityForResult(intent, SEARCH_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
