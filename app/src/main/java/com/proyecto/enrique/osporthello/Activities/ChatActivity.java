package com.proyecto.enrique.osporthello.Activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Adapters.MessagesAdapter;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.ImageManager;
import com.proyecto.enrique.osporthello.LocalDataBase;
import com.proyecto.enrique.osporthello.Models.Chat;
import com.proyecto.enrique.osporthello.Models.Message;
import com.proyecto.enrique.osporthello.Models.User;
import com.proyecto.enrique.osporthello.Services.ChatNotificationsService;
import com.proyecto.enrique.osporthello.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public static Chat CHAT;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private User USER_ME;
    private ArrayList<Message> messagesList = null;
    private LocalDataBase dataBase;
    private Firebase refChild;
    private ChildEventListener childEventListener;
    private boolean deletePair = false;

    CircleImageView toolbarImage;
    TextView toolbarUsername;
    EditText etxMessage;
    private static final String PREFERENCES_FILE = "osporthello_settings";
    private static final String SESSION_FILE = "my_session";

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
        if(getIntent().getExtras().containsKey("deletePair"))
            deletePair = true;

        // Set user chat info
        toolbarUsername.setText(this.CHAT.getReceiver_name());
        if(this.CHAT.getReceiver_image() != null && !this.CHAT.getReceiver_image().equals(""))
            toolbarImage.setImageBitmap(ImageManager.stringToBitMap(this.CHAT.getReceiver_image()));

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

        // Get my user info
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SESSION_FILE, 0);
        this.USER_ME = new User(sharedPreferences.getString("email", null),
                sharedPreferences.getString("firstname", null),
                sharedPreferences.getString("lastname", null), null,
                sharedPreferences.getString("apikey", null),
                sharedPreferences.getString("age", null),
                sharedPreferences.getString("city", null),
                sharedPreferences.getString("weight", null),
                sharedPreferences.getString("height", null));

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
        stopService(new Intent(ChatActivity.this, ChatNotificationsService.class));

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(this.CHAT.getId());

        // Get messages
        getChatMessages();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPref = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        if(sharedPref.getInt("chatnotifications", 0) != 0)
            startService(new Intent(ChatActivity.this, ChatNotificationsService.class));
        this.refChild.removeEventListener(childEventListener);
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
        adapter = new MessagesAdapter(messagesList, this);
        recycler.setAdapter(adapter);
    }

    /**
     * Send message to Firebase buffer
     */
    private void sendMessage() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
        Date now = new Date();
        String id = String.valueOf(this.CHAT.getId());
        String date = dateFormatter.format(now);
        String time = timeFormatter.format(now);

        String myEmail = this.USER_ME.getEmail();
        myEmail = myEmail.replace('.','0');
        myEmail = myEmail.replace('$','1');
        myEmail = myEmail.replace('#','2');
        myEmail = myEmail.replace('[','3');
        myEmail = myEmail.replace(']','4');
        myEmail = myEmail.replace('/','5');
        // Escribo mis mensajes en mi buffer de escritura, que se identifica con mi email
        Firebase.setAndroidContext(this);
        Firebase firebaseRoot = new Firebase("https://osporthello.firebaseio.com/");
        Firebase refChat = firebaseRoot.child("messages").child(id).child(myEmail);
        Message message = new Message();
        message.setAuthor(this.USER_ME.getEmail());
        message.setDate(date);
        message.setHour(time);
        message.setText(etxMessage.getText().toString());
        refChat.push().setValue(message);

        dataBase.insertNewMessage(message, Integer.valueOf(id));
        updateMessages();

        ApiClient.postNewMessage("api/chats/newmessage/" + this.CHAT.getId() + "/" + this.USER_ME.getEmail() + "/" + this.CHAT.getReceiver_email() + "/" + this.USER_ME.getApiKey(),
                new JsonHttpResponseHandler());
    }

    private Firebase getChatMessages() {
        final String id = String.valueOf(this.CHAT.getId());

        String receiverEmail = this.CHAT.getReceiver_email();
        receiverEmail = receiverEmail.replace('.','0');
        receiverEmail = receiverEmail.replace('$','1');
        receiverEmail = receiverEmail.replace('#','2');
        receiverEmail = receiverEmail.replace('[','3');
        receiverEmail = receiverEmail.replace(']','4');
        receiverEmail = receiverEmail.replace('/','5');
        // Leo de mi zona del chat, que es mi buffer de lectura y el de escritura de mi interlocutor
        Firebase.setAndroidContext(this);
        Firebase firebaseRoot = new Firebase("https://osporthello.firebaseio.com/");
        final Firebase refChat = firebaseRoot.child("messages").child(id).child(receiverEmail);

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

    private void deletePairChat() {
        ApiClient.deletePairChat("api/chats/pair/" + this.USER_ME.getEmail() + "/" + this.CHAT.getReceiver_email() + "/" + this.USER_ME.getApiKey(), new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("ERROR", "Error!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("chatslist", messagesList);
        outState.putBoolean("deletePair", deletePair);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        messagesList = (ArrayList<Message>) savedInstanceState.getSerializable("chatslist");
        deletePair = savedInstanceState.getBoolean("deletePair");
        updateMessages();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(deletePair && messagesList.isEmpty())
                    deletePairChat();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(deletePair && messagesList.isEmpty())
            deletePairChat();
    }
}
