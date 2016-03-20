package com.proyecto.enrique.osporthello;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by enrique on 20/03/16.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    private Context context;
    private ArrayList<User> items;

    // Constructor
    public FriendsAdapter(Context context, ArrayList<User> friends) {
        this.context = context;
        this.items = friends;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_cardview, viewGroup, false);
        return new FriendViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder viewHolder, int i) {
        String email = items.get(i).getEmail();
        String lastname = (items.get(i).getLastname() != null)?items.get(i).getLastname():"";
        String city = (items.get(i).getCity() != null)?items.get(i).getCity():"";
        viewHolder.image.setImageBitmap(stringToBitMap(items.get(i).getImage()));
        viewHolder.name.setText(items.get(i).getFirstname() + " " + lastname);
        viewHolder.city.setText(city);

        viewHolder.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: OPEN ACTIVITY CHAT WITH SELECTED USER
            }
        });
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

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView image;
        public TextView name;
        public TextView city;
        public Button btnChat;

        public FriendViewHolder(View v) {
            super(v);
            image = (CircleImageView) v.findViewById(R.id.user_image);
            name = (TextView) v.findViewById(R.id.name);
            city = (TextView) v.findViewById(R.id.city);
            btnChat = (Button)v.findViewById(R.id.btnChat);
        }
    }
}
