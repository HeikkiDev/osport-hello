package com.proyecto.enrique.osporthello.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.proyecto.enrique.osporthello.Activities.ChatActivity;
import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.User;

import java.util.ArrayList;

/**
 * Created by enrique on 12/04/16.
 */
public class NotificationsService extends Service {

    private static final String SESSION_FILE = "my_session";

    private Firebase refNotifications;
    private static ArrayList<ChildEventListener> listenersList;

    public NotificationsService(){
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "Servicio NOTIFICACIONES arrancado!", Toast.LENGTH_SHORT).show();
        //
        Firebase.setAndroidContext(this);
        listenersList = new ArrayList<>();

        addFirebaseListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Servicio NOTIFICACIONES parado!", Toast.LENGTH_SHORT).show();
        //
        for (ChildEventListener listener : listenersList) {
            this.refNotifications.removeEventListener(listener);
        }
        listenersList = null;
    }

    /**
     *
     */
    private void addFirebaseListener(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SESSION_FILE, 0);
        User user = new User(sharedPreferences.getString("email", null),
                sharedPreferences.getString("firstname", null),
                sharedPreferences.getString("lastname", null), null,
                sharedPreferences.getString("apikey", null),
                sharedPreferences.getString("sex", null),
                sharedPreferences.getString("age", null),
                sharedPreferences.getString("city", null),
                sharedPreferences.getString("weight", null),
                sharedPreferences.getString("height", null));

        String[] arrEmail = user.getEmail().split("\\.");
        String myEmail = arrEmail[0] + arrEmail[1];

        Firebase firebase = new Firebase("https://osporthello.firebaseio.com/");
        refNotifications = firebase.child("friends").child(myEmail);

        ChildEventListener childEventListener = refNotifications.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // New message from my interlocutor
                dataSnapshot.getRef().removeValue();
                String email = dataSnapshot.getValue(String.class);
                notifyNewFriend(email);
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
        listenersList.add(childEventListener);
    }

    /**
     *
     */
    private void notifyNewFriend(String email){
        final Context context = getApplicationContext();

        // Notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setContentTitle(email + " now follows you");

        Intent resultIntent = new Intent(context, MainActivity.class);

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

        mNotificationManager.notify(email.hashCode(), mBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
