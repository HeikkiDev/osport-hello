package com.proyecto.enrique.osporthello.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.proyecto.enrique.osporthello.Activities.ChatActivity;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.AnalyzeJSON;
import com.proyecto.enrique.osporthello.ApiClient;
import com.proyecto.enrique.osporthello.LocalDataBase;
import com.proyecto.enrique.osporthello.Models.Chat;
import com.proyecto.enrique.osporthello.Models.Message;
import com.proyecto.enrique.osporthello.Models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class ChatNotificationsService extends Service {

    private Firebase refChats;
    private Timer timer;
    private TimerTask timerTask;

    //private int idNotification = 0;
    private Handler mHandler = new Handler();
    ArrayList<Chat> listChats;
    ArrayList<ChildEventListener> listFirebaseListeners = new ArrayList<>();

    public ChatNotificationsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listChats = new ArrayList<>();

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // corre en otro hilo
                mHandler.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                // Check for new user's chats
                                checkUserChats();
                            }
                        }
                );
            }
        };
        timer.schedule(timerTask, 0, 20000); // 20 seconds
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (ChildEventListener listener : listFirebaseListeners) {
            this.refChats.removeEventListener(listener);
        }

        listChats = null;
        listFirebaseListeners = null;

        if(timer != null)
            timer.cancel();
    }

    /**
     *
     */
    private void checkUserChats() {
        try {
            User user = MainActivity.USER_ME;
            ApiClient.getUserChats("api/chats/" + user.getEmail(), new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("CHAT_NOTIFICATIONS", "ERROR!!");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        LocalDataBase dataBase = new LocalDataBase(getApplicationContext());
                        if (!response.getString("data").equals("null")) {
                            listChats = AnalyzeJSON.analyzeChats(response);
                            dataBase.insertChatList(listChats);
                        } else {
                            listChats.clear();
                            dataBase.insertChatList(listChats);
                        }

                        addChatsListener();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e){
            Log.e("CHAT_NOTIFICATIONS", "ERROR!!");
        }
    }

    /**
     *
     */
    private void addChatsListener() {
        LocalDataBase dataBase = new LocalDataBase(getApplicationContext());
        Cursor cursor = dataBase.getMyChats(MainActivity.USER_ME.getEmail());
        ArrayList<Chat> localListChats = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Chat chat = new Chat();
                chat.setId(cursor.getInt(cursor.getColumnIndex(LocalDataBase.CHAT_ID)));
                chat.setReceiver_email(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_RECEIVER)));
                chat.setReceiver_name(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_NAME)));
                chat.setReceiver_image(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_IMAGE)));
                localListChats.add(chat);
            } while (cursor.moveToNext());
        }
        dataBase.Close();

        try {
            // Remove previous listeners
            for (ChildEventListener listener : listFirebaseListeners) {
                this.refChats.removeEventListener(listener);
            }
            listFirebaseListeners.clear();
        }
        catch (Exception e){Log.e("CHAT_NOTIFICATIONS", "ERROR!!");}

        // Add Firebase listeners
        for (Chat chat : localListChats) {
            addFirebaseListener(chat);
        }
    }

    /**
     *
     * @param chat
     */
    private void addFirebaseListener(final Chat chat){
        final String id = String.valueOf(chat.getId());

        String[] arrEmail = chat.getReceiver_email().split("\\.");
        String receiverEmail = arrEmail[0] + arrEmail[1];
        // Leo de mi zona del chat, que es mi buffer de lectura y el de escritura de mi interlocutor
        refChats = MainActivity.FIREBASE.child("messages").child(id).child(receiverEmail);

        ChildEventListener childEventListener = refChats.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // New message from my interlocutor
                notifyNewMessage(chat);
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

        this.listFirebaseListeners.add(childEventListener);
    }

    /**
     *
     */
    private void notifyNewMessage(Chat chat){
        final Context context = getApplicationContext();

        // Notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setContentTitle("New merssage in chat "+chat.getId()+" from "+chat.getReceiver_email());

        Intent resultIntent = new Intent(context, ChatActivity.class);
        resultIntent.putExtra("myChat", chat);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(ChatActivity.class);

        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(chat.getId(), mBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
