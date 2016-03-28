package com.proyecto.enrique.osporthello;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public static Chat CHAT;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private ArrayList<Message> messagesList = null;
    private LocalDataBase dataBase;
    private Firebase refChild;
    private ChildEventListener childEventListener;

    CircleImageView toolbarImage;
    TextView toolbarUsername;
    EditText etxMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        messagesList = new ArrayList<>();
        dataBase = new LocalDataBase(this);
        toolbarImage = (CircleImageView)toolbar.findViewById(R.id.user_image);
        toolbarUsername = (TextView)toolbar.findViewById(R.id.name);
        etxMessage = (EditText)findViewById(R.id.etxSendMessage);

        // Obtain info about this chat
        this.CHAT = (Chat) getIntent().getSerializableExtra("myChat");

        // Set user chat info
        toolbarUsername.setText(this.CHAT.getReceiver_name());
        toolbarImage.setImageBitmap(stringToBitMap(this.CHAT.getReceiver_image()));

        // Obtain Recycler
        recycler = (RecyclerView) findViewById(R.id.recyclerViewMessages);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        lManager = linearLayoutManager;
        recycler.setLayoutManager(lManager);

        // To show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateMessages();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send message
                if(!etxMessage.getText().toString().isEmpty()) {
                    sendMessage();
                    etxMessage.setText("");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get messages
        getChatMessages();
    }

    private void updateMessages(){
        Cursor cursor = dataBase.getMessages(this.CHAT.getId());
        if (cursor.moveToFirst()) {
            messagesList.clear();
            do {
                Message message = new Message();
                message.setAuthor(cursor.getString(cursor.getColumnIndex(LocalDataBase.MESSAGE_AUTHOR)));
                message.setDate(cursor.getString(cursor.getColumnIndex(LocalDataBase.MESSAGE_DATE)));
                message.setHour(cursor.getString(cursor.getColumnIndex(LocalDataBase.MESSAGE_TIME)));
                message.setText(cursor.getString(cursor.getColumnIndex(LocalDataBase.MESSAGE_TEXT)));
                messagesList.add(message);
            } while (cursor.moveToNext());
        }
        adapter = new MessagesAdapter(messagesList);
        recycler.setAdapter(adapter);
    }

    /**
     * Send message to Firebase buffer
     */
    private void sendMessage() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();
        String id = String.valueOf(this.CHAT.getId());
        String date = dateFormatter.format(now);
        String time = timeFormatter.format(now);

        String[] arrEmail = MainActivity.USER_ME.getEmail().split("\\.");
        String myEmail = arrEmail[0] + arrEmail[1];
        // Escribo mis mensajes en mi buffer de escritura, que se identifica con mi email
        Firebase refChat = MainActivity.FIREBASE.child("messages").child(id).child(myEmail);
        Message message = new Message();
        message.setAuthor(MainActivity.USER_ME.getEmail());
        message.setDate(date);
        message.setHour(time);
        message.setText(etxMessage.getText().toString());
        refChat.push().setValue(message);

        dataBase.insertNewMessage(message, Integer.valueOf(id));
        updateMessages();
    }

    private Firebase getChatMessages() {
        final String id = String.valueOf(this.CHAT.getId());

        String[] arrEmail = this.CHAT.getReceiver_email().split("\\.");
        String receiverEmail = arrEmail[0] + arrEmail[1];
        // Leo de mi zona del chat, que es mi buffer de lectura y el de escritura de mi interlocutor
        final Firebase refChat = MainActivity.FIREBASE.child("messages").child(id).child(receiverEmail);

        // Descarga UNA VEZ la lista completa de mensajes del chat
        refChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    data.getRef().removeValue();
                    Message message = data.getValue(Message.class);
                    dataBase.insertNewMessage(message, Integer.valueOf(id));
                }
                if (dataSnapshot.getValue() != null) {
                    // Update recylcerView messages
                    updateMessages();

                }
                // Listen for new Messages
                listenNewMessages(refChat, id);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                //
            }
        });

        return refChat;
    }

    private void listenNewMessages(final Firebase refChat, final String id){
        // Listen for new messages
        this.refChild = refChat;
        this.childEventListener = refChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                dataSnapshot.getRef().removeValue();
                Message message = dataSnapshot.getValue(Message.class);
                dataBase.insertNewMessage(message, Integer.valueOf(id));

                // Update recylcerView messages
                updateMessages();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

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

    @Override
    protected void onStop() {
        super.onStop();
        this.refChild.removeEventListener(childEventListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("chatslist", messagesList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        messagesList = (ArrayList<Message>) savedInstanceState.getSerializable("chatslist");
        updateMessages();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
