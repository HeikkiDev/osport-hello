package com.proyecto.enrique.osporthello;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by enrique on 25/03/16.
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {
    private ArrayList<Chat> items;

    // Constructor
    public ChatsAdapter(ArrayList<Chat> chats) {
        this.items = chats;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_cardview, viewGroup, false);
        ChatViewHolder.IMyViewHolderClick viewHolderClick = new ChatViewHolder.IMyViewHolderClick() {
            @Override
            public void onItemClick(View v, ChatViewHolder viewHolder) {
                // TODO: LANZAR NUEVA ACTIVITY DE CHAT
                // Se cogen los datos sobre el chat con:
                //items.get(viewHolder.getAdapterPosition()).getReceiver_name();
            }
        };
        return new ChatViewHolder(v, viewHolderClick);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder viewHolder, int i) {
        String name = items.get(i).getReceiver_name();
        String image = items.get(i).getGetReceiver_image();

        viewHolder.name.setText(name);
        viewHolder.image.setImageBitmap(stringToBitMap(image));
        //viewHolder.notification.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
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

    static class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CircleImageView image;
        public TextView name;
        public ImageView notification;

        private ChatViewHolder viewHolder;
        public IMyViewHolderClick mListener;

        public ChatViewHolder(View v, IMyViewHolderClick listener) {
            super(v);
            viewHolder = this;
            mListener = listener;

            image = (CircleImageView) v.findViewById(R.id.user_image);
            name = (TextView) v.findViewById(R.id.name);
            notification = (ImageView)v.findViewById(R.id.notification);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(v, viewHolder);
        }

        public static interface IMyViewHolderClick {
            public void onItemClick(View v, ChatViewHolder viewHolder);
        }
    }
}
