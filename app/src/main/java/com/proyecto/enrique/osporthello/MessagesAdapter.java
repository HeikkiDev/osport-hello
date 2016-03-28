package com.proyecto.enrique.osporthello;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by enrique on 25/03/16.
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private ArrayList<Message> items;

    // Constructor
    public MessagesAdapter(ArrayList<Message> messages) {
        this.items = messages;
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

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (author.equals(MainActivity.USER_ME.getEmail())) {
            params.gravity = Gravity.RIGHT;
            viewHolder.cardView.setCardBackgroundColor(viewHolder.cardView.getContext().getResources().getColor(android.R.color.white));
        }
        else{
            params.gravity = Gravity.LEFT;
            viewHolder.cardView.setCardBackgroundColor(viewHolder.cardView.getContext().getResources().getColor(R.color.primaryColor));
        }
        viewHolder.cardView.setLayoutParams(params);
        viewHolder.message.setText(message);
        viewHolder.datetime.setText(datetime);
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
