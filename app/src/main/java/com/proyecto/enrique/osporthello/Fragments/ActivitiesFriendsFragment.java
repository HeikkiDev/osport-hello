package com.proyecto.enrique.osporthello.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.proyecto.enrique.osporthello.R;

public class ActivitiesFriendsFragment extends Fragment {

    private ProgressBar progressBar;

    public ActivitiesFriendsFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities_friends, container, false);
        progressBar = (ProgressBar)view.findViewById(R.id.progressActivitiesFriends);
        progressBar.setVisibility(View.GONE);

        if(savedInstanceState == null)
            progressBar.setVisibility(View.VISIBLE);

        return view;
    }
}
