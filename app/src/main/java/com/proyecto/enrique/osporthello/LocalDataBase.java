package com.proyecto.enrique.osporthello;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.proyecto.enrique.osporthello.Activities.MainActivity;
import com.proyecto.enrique.osporthello.Models.Chat;
import com.proyecto.enrique.osporthello.Models.Message;

import java.util.ArrayList;

/**
 * Created by enrique on 27/03/16.
 */
public class LocalDataBase {

    private Context miContexto = null;
    private DataBaseHelperInternal miHelperInternal = null;
    private SQLiteDatabase miDB = null;
    private static final String DATABASE_NAME = "osporthello";
    private static final int DATABASE_VERSION = 4;

    // Tables and columns
    private static final String TABLE_CHAT = "chat";
    public static final String CHAT_ID = "_id";
    public static final String CHAT_MYEMAIL = "Chat_me";
    public static final String CHAT_RECEIVER = "Chat_receiver";
    public static final String CHAT_NAME = "Chat_name";
    public static final String CHAT_IMAGE = "Chat_image";

    private static final String TABLE_MESSAGE = "message";
    public static final String MESSAGE_ID = "_id";
    public static final String MESSAGE_CHATID = "chat_id";
    public static final String MESSAGE_AUTHOR = "author";
    public static final String MESSAGE_DATE = "date";
    public static final String MESSAGE_TIME = "time";
    public static final String MESSAGE_TEXT = "text";

    // Constructor
    public  LocalDataBase(Context context){
        this.miContexto = context;
        this.Open();
    }

    // Open database
    private LocalDataBase Open(){
        miHelperInternal = new DataBaseHelperInternal(miContexto);
        miDB = miHelperInternal.getWritableDatabase();
        //miDB.execSQL("PRAGMA foreign_keys = ON"); // delete cascade doesn't work without this
        return this;
    }

    // Close database
    public void Close(){
        miHelperInternal.close();
    }

    public Cursor getMyChats(String myEmail){
        String mSql = "select "+
                CHAT_ID + ", "+
                CHAT_RECEIVER +", "+
                CHAT_NAME +", "+
                CHAT_IMAGE +
                " from " +
                TABLE_CHAT +
                " where "+CHAT_MYEMAIL+" =?";
        return miDB.rawQuery(mSql,
                new String[]{myEmail});
    }

    // Insert chat
    public long insertNewChat(int chat_id, String receiverEmail, String name, String image){
        ContentValues values = new ContentValues();
        values.put(CHAT_ID, chat_id);
        values.put(CHAT_MYEMAIL, MainActivity.USER_ME.getEmail());
        values.put(CHAT_RECEIVER, receiverEmail);
        values.put(CHAT_NAME, name);
        values.put(CHAT_IMAGE, image);

        return miDB.insert(TABLE_CHAT, null, values);
    }

    // Insert chat list
    public void insertChatList(ArrayList<Chat> chatList, String userEmail){
        // Delete all table
        long deleted = miDB.delete(TABLE_CHAT, null, new String[]{});
        // Populate table
        for (Chat chat:chatList) {
            ContentValues values = new ContentValues();
            values.put(CHAT_ID, chat.getId());
            values.put(CHAT_MYEMAIL, userEmail);
            values.put(CHAT_RECEIVER, chat.getReceiver_email());
            values.put(CHAT_NAME, chat.getReceiver_name());
            values.put(CHAT_IMAGE, chat.getReceiver_image());
            miDB.insert(TABLE_CHAT, null, values);
        }
    }

    // Insert chat list in services
    public void insertChatsInService(ArrayList<Chat> chatList, String userEmail){
        ArrayList<Chat> listChats = new ArrayList<>();
        Cursor cursor = getMyChats(userEmail);

        if (cursor.moveToFirst()) {
            do {
                Chat chat = new Chat();
                chat.setId(cursor.getInt(cursor.getColumnIndex(LocalDataBase.CHAT_ID)));
                chat.setReceiver_email(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_RECEIVER)));
                chat.setReceiver_name(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_NAME)));
                chat.setReceiver_image(cursor.getString(cursor.getColumnIndex(LocalDataBase.CHAT_IMAGE)));
                listChats.add(chat);
            } while (cursor.moveToNext());
        }

        // Delete all table
        long deleted = miDB.delete(TABLE_CHAT, null, new String[]{});

        // Populate table
        for (Chat chat:chatList) {
            ContentValues values = new ContentValues();
            values.put(CHAT_ID, chat.getId());
            values.put(CHAT_MYEMAIL, userEmail);
            values.put(CHAT_RECEIVER, chat.getReceiver_email());
            values.put(CHAT_NAME, chat.getReceiver_name());
            values.put(CHAT_IMAGE, searchImage(listChats, chat.getId()));
            miDB.insert(TABLE_CHAT, null, values);
        }
    }

    private String searchImage(ArrayList<Chat> list, int chatId) {
        for (Chat chat:list) {
            if(chat.getId() == chatId)
                return chat.getReceiver_image();
        }
        return null;
    }

    // Delete chat
    public int deleteChat(String receiverEmail){
        return miDB.delete(TABLE_CHAT,
                CHAT_MYEMAIL +"=? and "+ CHAT_RECEIVER + "=?",
                new String[]{MainActivity.USER_ME.getEmail(), receiverEmail});
    }

    // Check if exists chat
    public boolean existsChatWith(String receiverEmail){
        Cursor cursor = miDB.rawQuery("select * from "+TABLE_CHAT+" where "+CHAT_MYEMAIL+"=? and "+CHAT_RECEIVER+"=?", new String[]{MainActivity.USER_ME.getEmail(), receiverEmail});
        return cursor.moveToFirst();
    }

    // Get specific chat messages
    public Cursor getMessages(int chatId){
        String mSql = "select * from " +
                TABLE_MESSAGE +
                " where " + MESSAGE_CHATID + "=?";
        return miDB.rawQuery(mSql,
                new String[]{String.valueOf(chatId)});
    }

    // Insert message
    public long insertNewMessage(Message message, int chatId){
        ContentValues values = new ContentValues();
        values.put(MESSAGE_CHATID, chatId);
        values.put(MESSAGE_AUTHOR, message.getAuthor());
        values.put(MESSAGE_DATE, message.getDate());
        values.put(MESSAGE_TIME, message.getHour());
        values.put(MESSAGE_TEXT, message.getText());

        return miDB.insert(TABLE_MESSAGE, null, values);
    }

    // Delete specific chat messages
    public int deleteMessages(int chatId){
        return miDB.delete(TABLE_MESSAGE,
                MESSAGE_CHATID + "=?",
                new String[]{String.valueOf(chatId)});
    }

    // SQL create tables
    private static final String DATABASE_CREATE_CHAT = "create table if not exists " + TABLE_CHAT + " (" +
            CHAT_ID + " integer, "+
            CHAT_MYEMAIL + " text not null, " +
            CHAT_RECEIVER + " text not null, " +
            CHAT_NAME + " text not null, " +
            CHAT_IMAGE + " blob, " +
            " primary key("+CHAT_ID+", "+CHAT_RECEIVER+"));";

    private static final String DATABASE_CREATE_MESSAGE = "create table if not exists " + TABLE_MESSAGE + " (" +
            MESSAGE_ID + " integer primary key autoincrement, "+
            MESSAGE_CHATID + " integer, "+
            MESSAGE_AUTHOR + " text not null, " +
            MESSAGE_DATE + " text not null, " +
            MESSAGE_TIME + " text not null, " +
            MESSAGE_TEXT + " text not null," +
            "foreign key ("+MESSAGE_CHATID+") references "+TABLE_CHAT+"("+CHAT_ID+") on delete cascade);";

    private static class DataBaseHelperInternal extends SQLiteOpenHelper {

        public DataBaseHelperInternal(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        private void createTable(SQLiteDatabase db){
            db.execSQL(DATABASE_CREATE_CHAT);
            db.execSQL(DATABASE_CREATE_MESSAGE);
        }
    }

}
