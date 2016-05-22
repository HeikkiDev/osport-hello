package com.proyecto.enrique.osporthello.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Activities.ChatActivity;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.ImageManager;
import com.proyecto.enrique.osporthello.Interfaces.UserInfoInterface;
import com.proyecto.enrique.osporthello.LocalDataBase;
import com.proyecto.enrique.osporthello.Models.Chat;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.NameAndImageTask;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by enrique on 20/03/16.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    private Context context;
    private ArrayList<User> items;
    private FriendsChanges myInterface;
    private UserInfoInterface infoInterface;

    public interface FriendsChanges
    {
        void onFriendsChanges();
    }

    // Constructor
    public FriendsAdapter(Context context, FriendsChanges interf, UserInfoInterface info, ArrayList<User> friends) {
        this.context = context;
        this.myInterface = interf;
        this.infoInterface = info;
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
    public void onBindViewHolder(final FriendViewHolder viewHolder, final int i) {
        if(items.get(i).getImage() == null || items.get(i).getImage().equals(""))
            new NameAndImageTask(items.get(i).getEmail(), viewHolder.name, viewHolder.image, i, infoInterface).execute();
        else{
            viewHolder.name.setText(items.get(i).getFirstname()+" "+((items.get(i).getLastname()!=null)?items.get(i).getLastname():""));
            viewHolder.image.setImageBitmap(ImageManager.stringToBitMap(items.get(i).getImage()));
        }
        String email = items.get(i).getEmail();
        String lastname = (items.get(i).getLastname() != null)?items.get(i).getLastname():"";
        final String city = (items.get(i).getCity() != null)?items.get(i).getCity():"";
        //viewHolder.image.setImageBitmap(stringToBitMap(items.get(i).getImage()));
        viewHolder.name.setText(items.get(i).getFirstname() + " " + lastname);
        viewHolder.city.setText(city);

        viewHolder.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.btnChat.setEnabled(false);
                User user = items.get(i);
                // Create and/or open a Chat with a User
                newChat(user, viewHolder.btnChat);
            }
        });

        viewHolder.friendCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle(context.getResources().getString(R.string.unfollow_dialog))
                        .setMessage(R.string.sure_about_unfollow)
                        .setPositiveButton(context.getResources().getString(R.string.unfollow_dialog), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFriend(i);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }
        });
    }

    private void newChat(final User user, final Button btn) {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(6000);
        client.post(MainActivity.HOST + "api/chats/" + MainActivity.USER_ME.getEmail() + "/" + user.getEmail() + "/" + MainActivity.USER_ME.getApiKey(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("NEW_CHAT", "ERROR!!");
                btn.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                btn.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                btn.setEnabled(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        int chat_id = response.getInt("data");

                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("myChat", new Chat(chat_id, user.getEmail(), user.getFirstname() + user.getLastname(), user.getImage()));
                        context.startActivity(intent);
                    }
                    btn.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    btn.setEnabled(true);
                }
            }
        });
    }

    private void deleteFriend(final int i) {
        final FriendsAdapter friendsAdapter = this;
        User user = MainActivity.USER_ME;
        ApiClient.deleteFriend("api/friends/" + user.getEmail() + "/" + items.get(i).getEmail(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("DELETE_FRIEND", "ERROR!!");
                myInterface.onFriendsChanges();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        items.remove(i);
                        friendsAdapter.notifyDataSetChanged();
                    }
                    myInterface.onFriendsChanges();
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

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        public CardView friendCardView;
        public CircleImageView image;
        public TextView name;
        public TextView city;
        public Button btnChat;

        public FriendViewHolder(View v) {
            super(v);

            friendCardView = (CardView)v.findViewById(R.id.friendCardView);
            image = (CircleImageView) v.findViewById(R.id.user_image);
            name = (TextView) v.findViewById(R.id.name);
            city = (TextView) v.findViewById(R.id.city);
            btnChat = (Button)v.findViewById(R.id.btnChat);
        }

    }
}
