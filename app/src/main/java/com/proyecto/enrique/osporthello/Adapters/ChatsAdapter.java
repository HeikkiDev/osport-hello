package com.proyecto.enrique.osporthello.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Activities.ChatActivity;
import com.proyecto.enrique.osporthello.ImageManager;
import com.proyecto.enrique.osporthello.LocalDataBase;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.Chat;
import com.proyecto.enrique.osporthello.R;

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
                viewGroup.setEnabled(false);
                Chat chat = items.get(viewHolder.getAdapterPosition());
                Intent intent = new Intent(context, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("myChat", chat);
                context.startActivity(intent);
                viewGroup.setEnabled(true);
            }

            @Override
            public void onLongItemClick(final View v, final ChatViewHolder viewHolder) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle(context.getResources().getString(R.string.delete_chat))
                        .setMessage(context.getResources().getString(R.string.sure_delete_chat))
                        .setPositiveButton(context.getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Chat chat = items.get(viewHolder.getAdapterPosition());
                                deleteChat(v, chat); // Delete chat in remote database
                                LocalDataBase dataBase = new LocalDataBase(context);
                                dataBase.deleteChat(chat.getReceiver_email());
                                dataBase.Close();
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
        if(image != null && !image.equals(""))
            viewHolder.image.setImageBitmap(ImageManager.stringToBitMap(image));
        //viewHolder.notification.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void deleteChat(final View v, final Chat chat) {
        ApiClient.deleteChat("api/chats/" + MainActivity.USER_ME.getEmail() + "/" + chat.getReceiver_email(), new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("ERROR", "Error!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        Snackbar.make(v, "Chat deleted", Snackbar.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
