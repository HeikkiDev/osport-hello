package com.proyecto.enrique.osporthello.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.proyecto.enrique.osporthello.Models.RowActivity;
import com.proyecto.enrique.osporthello.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Adaptador para mostrar la lista de actividades deportivas diferentes en un diálogo personalizado.
 */

public class ChooseActivityAdapter extends ArrayAdapter<RowActivity> {

    private Activity context;
    private ArrayList<RowActivity> activitiesList;

    public ChooseActivityAdapter(Activity context, int resource, ArrayList<RowActivity> objects) {
        super(context, resource, objects);
        this.context = context;
        this.activitiesList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.row_activities, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.txtName = (TextView) view.findViewById(R.id.txtName);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.imageView);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.txtName.setText(activitiesList.get(position).getName());
        viewHolder.imageView.setImageDrawable(activitiesList.get(position).getImage());
        return view;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView txtName;
    }
}
