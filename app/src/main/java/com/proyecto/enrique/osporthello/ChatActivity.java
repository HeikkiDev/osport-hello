package com.proyecto.enrique.osporthello;

import android.content.Context;
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
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Chat CHAT;
    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private ArrayList<Message> messagesList = null;

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
        toolbarImage = (CircleImageView)toolbar.findViewById(R.id.user_image);
        toolbarUsername = (TextView)toolbar.findViewById(R.id.name);
        etxMessage = (EditText)findViewById(R.id.etxSendMessage);

        // Set user chat info
        toolbarUsername.setText(this.CHAT.getReceiver_name());
        toolbarImage.setImageBitmap(stringToBitMap(this.CHAT.getGetReceiver_image()));

        // Obtain info about this chat
        Bundle extras = getIntent().getExtras();
        this.CHAT = (Chat)extras.getSerializable("myChat");

        // Obtain Recycler
        recycler = (RecyclerView) findViewById(R.id.recyclerViewMessages);
        recycler.setHasFixedSize(true);

        // LinearLayout administrator
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        // To show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get messages
        getChatMessages();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send message
                if(!etxMessage.getText().toString().isEmpty())
                    sendMessage();
            }
        });
    }

    private void updateMessages(){
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

        // Escribo mis mensajes en mi buffer de escritura, que se identifica con mi email
        Firebase refChat = MainActivity.FIREBASE.child("messages").child(id).child(MainActivity.USER_ME.getEmail());
        Message mensaje = new Message();
        mensaje.setAuthor(MainActivity.USER_ME.getEmail());
        mensaje.setDate(date);
        mensaje.setHour(time);
        mensaje.setText(etxMessage.getText().toString());
        refChat.push().setValue(mensaje);
    }

    private void getChatMessages() {
        String id = String.valueOf(this.CHAT.getId());

        // TODO: LEER LOS ÚLTIMOS MENSAJES DE LA BASE DE DATOS INTERNA SQLITE Y AÑADIR A LA LISTA

        // Leo de mi zona del chat, que es mi buffer de lectura y el de escritura de mi interlocutor
        final Firebase refChat = MainActivity.FIREBASE.child("messages").child(id).child(this.CHAT.getReceiver_email());

        // Descarga UNA VEZ la lista completa de mensajes del chat
        refChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Message mensaje = data.getValue(Message.class);
                    messagesList.add(mensaje);
                }
                // Limpio el buffer para que la próxima vez que que salte este método me lleguen sólo mensajes nuevos
                refChat.removeValue();

                // Listen for new Messages
                listenNewMessages(refChat);

                // Update recylcerView messages
                updateMessages();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                //
            }
        });
    }

    private void listenNewMessages(final Firebase refChat){
        // Listen for new messages
        refChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Message mensaje = data.getValue(Message.class);
                    messagesList.add(mensaje);
                }
                // Limpio el buffer para que la próxima vez que que salte este método me lleguen sólo mensajes nuevos
                refChat.removeValue();

                // Update recylcerView messages
                updateMessages();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                //
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
