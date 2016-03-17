package com.proyecto.enrique.osporthello;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by enrique on 16/03/16.
 */
public class ActivitiesFragment extends Fragment {

    public ActivitiesFragment(){
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);

        return view;
    }
}
