package com.proyecto.enrique.osporthello.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.Activities.ChatActivity;
import com.proyecto.enrique.osporthello.ImageManager;
import com.proyecto.enrique.osporthello.LocalDataBase;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.Chat;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.AsyncTask.NameAndImageTask;
import com.proyecto.enrique.osporthello.R;
import com.proyecto.enrique.osporthello.Interfaces.UserInfoInterface;

import org.json.JSONArray;
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
    private FriendsChanges myInterface;
    private UserInfoInterface infoInterface;

    public interface FriendsChanges
    {
        void onFriendsChanges(String friendEmail, boolean added);
    }

    // Constructor
    public UsersAdapter(Context context, FriendsChanges inter, UserInfoInterface info, ArrayList<User> items, ArrayList<User> friends) {
        this.context = context;
        this.myInterface = inter;
        this.infoInterface = info;
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
    public void onBindViewHolder(final UserViewHolder viewHolder, final int i) {
        if(items.get(i).getImage() == null || items.get(i).getImage().equals("")) {
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_account_circle_black_48dp);
            viewHolder.name.setText(items.get(i).getFirstname()+" "+((items.get(i).getLastname()!=null)?items.get(i).getLastname():""));
            viewHolder.image.setImageBitmap(bm);
        }
        else{
            viewHolder.name.setText(items.get(i).getFirstname()+" "+((items.get(i).getLastname()!=null)?items.get(i).getLastname():""));
            viewHolder.image.setImageBitmap(ImageManager.stringToBitMap(items.get(i).getImage()));
        }
        String email = items.get(i).getEmail();
        String lastname = (items.get(i).getLastname() != null)?items.get(i).getLastname():"";
        String city = (items.get(i).getCity() != null)?items.get(i).getCity():"";
        //viewHolder.image.setImageBitmap(ImageManager.stringToBitMap(items.get(i).getImage()));
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
            viewHolder.btnFriend.setText(context.getResources().getString(R.string.unfollow));
            viewHolder.btnFriend.setBackgroundResource(R.color.cancelColor);
            viewHolder.btnFriend.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_outline_white_24dp, 0, 0, 0);
        }

        final int position = i;
        viewHolder.btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button)v;
                String btnText = context.getResources().getString(R.string.follow);
                if(btn.getText().equals(btnText)){
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
                viewHolder.btnChat.setEnabled(false);
                User user = items.get(i);
                // Create and/or open a Chat with a User
                newChat(user, viewHolder.btnChat);
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
                        btn.setText(context.getResources().getString(R.string.unfollow));
                        btn.setBackgroundResource(R.color.cancelColor);
                        btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_outline_white_24dp, 0, 0, 0);

                        myFriends.add(items.get(i));
                        myInterface.onFriendsChanges(items.get(i).getEmail(), true);

                        // Notify new friend
                        String email = items.get(i).getEmail();
                        email = email.replace('.','0');
                        email = email.replace('$','1');
                        email = email.replace('#','2');
                        email = email.replace('[','3');
                        email = email.replace(']','4');
                        email = email.replace('/','5');
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

    private void deleteFriend(final int i, final Button btn) {
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
                        btn.setText(context.getResources().getString(R.string.follow));
                        btn.setBackgroundResource(R.color.primaryColor);
                        btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_add_white_24dp, 0, 0, 0);

                        User user = items.get(i);
                        for (int i = 0; i < myFriends.size(); i++) {
                            if(myFriends.get(i).getEmail().equals(user.getEmail())) {
                                myFriends.remove(i);
                                myInterface.onFriendsChanges(user.getEmail(), false);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void newChat(final User user, final Button btn) {
        ApiClient.postNewChat("api/chats/" + MainActivity.USER_ME.getEmail() + "/" + user.getEmail() + "/" + MainActivity.USER_ME.getApiKey(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
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
                    btn.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    btn.setEnabled(true);
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
