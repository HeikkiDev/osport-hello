package com.proyecto.enrique.osporthello;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by enrique on 20/03/16.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private ArrayList<User> items;

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView image;
        public TextView name;
        public TextView city;

        public UserViewHolder(View v) {
            super(v);
            image = (CircleImageView) v.findViewById(R.id.user_image);
            name = (TextView) v.findViewById(R.id.name);
            city = (TextView) v.findViewById(R.id.city);
        }
    }

    public UsersAdapter(ArrayList<User> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_cardview, viewGroup, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserViewHolder viewHolder, int i) {
        String lastname = (items.get(i).getLastname() != null)?items.get(i).getLastname():"";
        String city = (items.get(i).getCity() != null)?items.get(i).getCity():"";
        viewHolder.image.setImageBitmap(stringToBitMap(items.get(i).getImage()));
        viewHolder.name.setText(items.get(i).getFirstname()+" "+lastname);
        viewHolder.city.setText(city);
    }

    /**
     * String 64 base enconded to Bitmap
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap stringToBitMap(String encodedString){
        try{
            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }
}
