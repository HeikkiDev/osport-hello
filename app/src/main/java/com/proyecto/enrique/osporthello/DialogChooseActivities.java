package com.proyecto.enrique.osporthello;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.proyecto.enrique.osporthello.Adapters.ChooseActivityAdapter;
import com.proyecto.enrique.osporthello.Models.RowActivity;

import java.util.ArrayList;

/**
 * Created by enrique on 27/04/16.
 */
public class DialogChooseActivities extends DialogFragment {

    ListView listView;

    private String[] arrayActivities = getResources().getStringArray(R.array.array_activities);
    private String[] arrayIcons = getResources().getStringArray(R.array.icons_activities);

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // Use the Builder class for convenient dialog construction
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.choose_activity_dialog, null);

        ArrayList<RowActivity> itemsList = new ArrayList<>();
        for (int i = 0; i < arrayActivities.length; i++) {
            Drawable drawable = getResources().getDrawable(getResources()
                    .getIdentifier(arrayIcons[i], "drawable", getActivity().getPackageName()));
            RowActivity rowActivity = new RowActivity(drawable,arrayActivities[i]);
            itemsList.add(rowActivity);
        }

        ChooseActivityAdapter adapterActivities = new ChooseActivityAdapter(getActivity(), R.layout.row_activities, itemsList);
        listView = (ListView)layout.findViewById(android.R.id.list);
        listView.setAdapter(adapterActivities);

        builder.setView(layout).setTitle("Choose your activity");

        return builder.create();
    }
}
