package com.proyecto.enrique.osporthello.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.proyecto.enrique.osporthello.Activities.ChatActivity;
import com.proyecto.enrique.osporthello.Activities.MainActivity;

/**
 * Created by enrique on 12/04/16.
 */
public class NotificationsService extends Service {

    public NotificationsService(){
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //
        addFirebaseListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //
    }

    /**
     *
     */
    private void addFirebaseListener(){
        String[] arrEmail = MainActivity.USER_ME.getEmail().split("\\.");
        String myEmail = arrEmail[0] + arrEmail[1];

        Firebase refChats = MainActivity.FIREBASE.child("friends").child(myEmail);

        ChildEventListener childEventListener = refChats.addChildEventListener(new ChildEventListener() {
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
