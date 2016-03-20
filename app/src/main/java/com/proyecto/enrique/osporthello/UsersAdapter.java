package com.proyecto.enrique.osporthello;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by enrique on 20/03/16.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private Context context;
    private ArrayList<User> items;
    private ArrayList<User> myFriends;

    // Constructor
    public UsersAdapter(Context context, ArrayList<User> items, ArrayList<User> friends) {
        this.context = context;
        this.items = items;
        this.myFriends = friends;
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
        String email = items.get(i).getEmail();
        String lastname = (items.get(i).getLastname() != null)?items.get(i).getLastname():"";
        String city = (items.get(i).getCity() != null)?items.get(i).getCity():"";
        viewHolder.image.setImageBitmap(stringToBitMap(items.get(i).getImage()));
        viewHolder.name.setText(items.get(i).getFirstname()+" "+lastname);
        viewHolder.city.setText(city);

        boolean contains = false;
        if(myFriends != null) {
            for (User user : myFriends) {
                if (user.getEmail().equals(email)) {
                    contains = true;
                    break;
                }
            }
        }

        if(contains){
            viewHolder.btnFriend.setText("Unfollow");
            viewHolder.btnFriend.setBackgroundResource(R.color.cancelColor);
            viewHolder.btnFriend.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_outline_white_24dp, 0, 0, 0);
        }

        final int position = i;
        viewHolder.btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button)v;
                if(btn.getText().equals("Follow")){
                    makeNewFriend(position, btn);
                }
                else{
                    deleteFriend(position, btn);
                }
            }
        });

        viewHolder.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: OPEN ACTIVITY CHAT WITH SELECTED USER
            }
        });
    }

    private void makeNewFriend(int i, final Button btn) {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(10000);
        User user = MainActivity.USER_ME;
        client.post(MainActivity.HOST + "api/friends/" + user.getEmail() + "/"+items.get(i).getEmail()+"/" + user.getApiKey(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("FRIENDS", "ERROR!!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        btn.setText("Unfollow");
                        btn.setBackgroundResource(R.color.cancelColor);
                        btn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_person_outline_white_24dp, 0, 0, 0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void deleteFriend(int i, final Button btn) {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(10000);
        User user = MainActivity.USER_ME;
        client.delete(MainActivity.HOST + "api/friends/" + user.getEmail() + "/" + items.get(i).getEmail() + "/" + user.getApiKey(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("FRIENDS", "ERROR!!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        btn.setText("Follow");
                        btn.setBackgroundResource(R.color.primaryColor);
                        btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_add_white_24dp, 0, 0, 0);
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

    static class UserViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView image;
        public TextView name;
        public TextView city;
        public Button btnFriend;
        public Button btnChat;

        public UserViewHolder(View v) {
            super(v);
            image = (CircleImageView) v.findViewById(R.id.user_image);
            name = (TextView) v.findViewById(R.id.name);
            city = (TextView) v.findViewById(R.id.city);
            btnFriend = (Button)v.findViewById(R.id.btnMakeFriend);
            btnChat = (Button)v.findViewById(R.id.btnChat);
        }
    }
}
