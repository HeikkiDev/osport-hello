package com.proyecto.enrique.osporthello.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.Message;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by enrique on 25/03/16.
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private ArrayList<Message> items;
    private static User USER_ME;

    private static final String SESSION_FILE = "my_session";

    // Constructor
    public MessagesAdapter(ArrayList<Message> messages, Context context) {
        this.items = messages;
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(SESSION_FILE, 0);
        this.USER_ME = new User(sharedPreferences.getString("email", null),
                sharedPreferences.getString("firstname", null),
                sharedPreferences.getString("lastname", null), null,
                sharedPreferences.getString("apikey", null),
                sharedPreferences.getString("sex", null),
                sharedPreferences.getString("age", null),
                sharedPreferences.getString("city", null),
                sharedPreferences.getString("weight", null),
                sharedPreferences.getString("height", null));
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.messagerow_cardview, viewGroup, false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int i) {
        String author = items.get(i).getAuthor();
        String message = items.get(i).getText();
        String datetime = "";

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        if (items.get(i).getDate().equals(formatter.format(new Date())))
            datetime = items.get(i).getHour();
        else
            datetime = items.get(i).getDate() + " " + items.get(i).getHour();

        LinearLayout.LayoutParams paramsCardView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams paramsDatetime = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (author.equals(this.USER_ME.getEmail())) {
            paramsCardView.gravity = Gravity.RIGHT;
            paramsCardView.leftMargin = 200;
            paramsDatetime.gravity = Gravity.RIGHT;
            viewHolder.cardView.setCardBackgroundColor(viewHolder.cardView.getContext().getResources().getColor(android.R.color.white));
        }
        else{
            paramsCardView.gravity = Gravity.LEFT;
            paramsCardView.rightMargin = 200;
            paramsDatetime.gravity = Gravity.LEFT;
            viewHolder.cardView.setCardBackgroundColor(viewHolder.cardView.getContext().getResources().getColor(R.color.primaryColor));
        }
        viewHolder.cardView.setLayoutParams(paramsCardView);
        viewHolder.message.setText(message);
        viewHolder.datetime.setText(datetime);
        viewHolder.datetime.setLayoutParams(paramsDatetime);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView message;
        public TextView datetime;

        public MessageViewHolder(View v) {
            super(v);

            cardView = (CardView)v.findViewById(R.id.messageCardView);
            message = (TextView) v.findViewById(R.id.message);
            datetime = (TextView)v.findViewById(R.id.datetime);
        }

    }
}
