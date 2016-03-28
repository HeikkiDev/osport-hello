package com.proyecto.enrique.osporthello;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by enrique on 25/03/16.
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {
    private Context context;
    private ArrayList<Chat> items;
    private ChatsAdapter chatsAdapter;

    // Constructor
    public ChatsAdapter(Context context, ArrayList<Chat> chats) {
        this.context = context;
        this.items = chats;
        this.chatsAdapter = this;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_cardview, viewGroup, false);
        ChatViewHolder.IChatViewHolderClick viewHolderClick = new ChatViewHolder.IChatViewHolderClick() {
            @Override
            public void onItemClick(View v, ChatViewHolder viewHolder) {
                // Se cogen los datos sobre el chat con:
                //items.get(viewHolder.getAdapterPosition()).getReceiver_name();
                Chat chat = items.get(viewHolder.getAdapterPosition());
                Intent intent = new Intent(context, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("myChat", chat);
                context.startActivity(intent);
            }

            @Override
            public void onLongItemClick(final View v, final ChatViewHolder viewHolder) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete chat")
                        .setMessage("Are you sure about delete this chat?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Chat chat = items.get(viewHolder.getAdapterPosition());
                                deleteChat(v, chat); // Delete chat in remote database
                                items.remove(viewHolder.getAdapterPosition());
                                chatsAdapter.notifyDataSetChanged(); // refresh data
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        };
        return new ChatViewHolder(v, viewHolderClick);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder viewHolder, int i) {
        String name = items.get(i).getReceiver_name();
        String image = items.get(i).getReceiver_image();

        viewHolder.name.setText(name);
        viewHolder.image.setImageBitmap(stringToBitMap(image));
        //viewHolder.notification.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void deleteChat(final View v, final Chat chat) {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(6000);
        client.delete(MainActivity.HOST + "api/chats/" + MainActivity.USER_ME.getEmail() + "/"+ chat.getReceiver_email() + "/" + MainActivity.USER_ME.getApiKey(), new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("ERROR", "Error!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        LocalDataBase dataBase = new LocalDataBase(context);
                        dataBase.deleteChat(chat.getReceiver_email());
                        dataBase.Close();
                        Snackbar.make(v, "Chat deleted", Snackbar.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    static class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public CircleImageView image;
        public TextView name;
        public ImageView notification;

        private ChatViewHolder viewHolder;
        public IChatViewHolderClick mListener;

        public ChatViewHolder(View v, IChatViewHolderClick listener) {
            super(v);
            viewHolder = this;
            mListener = listener;

            image = (CircleImageView) v.findViewById(R.id.user_image);
            name = (TextView) v.findViewById(R.id.name);
            notification = (ImageView)v.findViewById(R.id.notification);

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(v, viewHolder);
        }

        @Override
        public boolean onLongClick(View v) {
            mListener.onLongItemClick(v, viewHolder);
            return true;
        }

        public static interface IChatViewHolderClick {
            public void onItemClick(View v, ChatViewHolder viewHolder);
            public void onLongItemClick(View v, ChatViewHolder viewHolder);
        }
    }
}
