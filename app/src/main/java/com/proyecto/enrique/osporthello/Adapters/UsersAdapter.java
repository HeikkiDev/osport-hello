package com.proyecto.enrique.osporthello.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Activities.ChatActivity;
import com.proyecto.enrique.osporthello.ImageManager;
import com.proyecto.enrique.osporthello.LocalDataBase;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.Chat;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;
import com.proyecto.enrique.osporthello.Activities.SearchUsersActivity;

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
    public void onBindViewHolder(UserViewHolder viewHolder, final int i) {
        String email = items.get(i).getEmail();
        String lastname = (items.get(i).getLastname() != null)?items.get(i).getLastname():"";
        String city = (items.get(i).getCity() != null)?items.get(i).getCity():"";
        viewHolder.image.setImageBitmap(ImageManager.stringToBitMap(items.get(i).getImage()));
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
                SearchUsersActivity.FRIENDS_CHANGE = true;
            }
        });

        viewHolder.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = items.get(i);
                // Create and/or open a Chat with a User
                newChat(user);
            }
        });
    }

    private void makeNewFriend(final int i, final Button btn) {
        final User user = MainActivity.USER_ME;
        ApiClient.postNewFriend("api/friends/" + user.getEmail() + "/" + items.get(i).getEmail(), new JsonHttpResponseHandler() {
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
                        btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_outline_white_24dp, 0, 0, 0);

                        // Notify new friend
                        String[] arrEmail = items.get(i).getEmail().split("\\.");
                        String email = arrEmail[0] + arrEmail[1];
                        Firebase.setAndroidContext(context);
                        Firebase firebaseRoot = new Firebase("https://osporthello.firebaseio.com/");
                        Firebase refChat = firebaseRoot.child("friends").child(email);
                        refChat.push().setValue(user.getEmail());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void deleteFriend(int i, final Button btn) {
        User user = MainActivity.USER_ME;
        ApiClient.deleteFriend("api/friends/" + user.getEmail() + "/" + items.get(i).getEmail(), new JsonHttpResponseHandler() {
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

    private void newChat(final User user) {
        ApiClient.postNewChat("api/chats/" + MainActivity.USER_ME.getEmail() + "/" + user.getEmail(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                //
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        int chat_id = Integer.valueOf(response.getString("data"));

                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        String lastname = (user.getLastname() != null)?user.getLastname():"";
                        intent.putExtra("myChat", new Chat(chat_id, user.getEmail(), user.getFirstname() + " " + lastname, user.getImage()));
                        context.startActivity(intent);

                        //if(!response.getString("message").equals("ALREADY EXISTS")){
                        // Insert in local database
                        LocalDataBase dataBase = new LocalDataBase(context);
                        long i = dataBase.insertNewChat(chat_id, user.getEmail(), user.getFirstname() + " " + user.getLastname(), user.getImage());
                        dataBase.Close();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
