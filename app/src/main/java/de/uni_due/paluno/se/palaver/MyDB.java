package de.uni_due.paluno.se.palaver;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;


public class MyDB {
    private myDbHelper myhelper;

    MyDB(Context context)
    {
        myhelper = new myDbHelper(context);
    }

    void insertUser(String nikName, String password)
    {
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("NikName", nikName);
        contentValues.put("Password", password);
        DB.insert("User", null , contentValues);
    }

    void updatePassword(String nikName, String password){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        DB.execSQL("UPDATE User SET Password=" + "'" + password+ "'" + " WHERE NikName LIKE " + "'" + nikName+ "'");
    }

    void setLoggedIn(String nikName, String password, int isLoggedIn){
       SQLiteDatabase DB = myhelper.getWritableDatabase();
       DB.execSQL("UPDATE User SET IsLoggedIN=" + "'" + isLoggedIn+ "'" + " WHERE NikName LIKE " + "'" + nikName+ "'");
       if(isLoggedIn == 1){
           MainActivity.nikName = nikName;
           MainActivity.password = password;
       }
       else{
           MainActivity.nikName = null;
           MainActivity.password = null;
       }
    }

    String[] getLoggedUser(){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT NikName, Password FROM User WHERE IsLoggedIn = 1", null);
        String[] loggedUser = new String[3];
        while (cursor.moveToNext())
        {
            loggedUser[0]= cursor.getString(cursor.getColumnIndex("NikName"));
            loggedUser[1]= cursor.getString(cursor.getColumnIndex("Password"));
        }
        return loggedUser;
    }

    boolean isValideUser(String nikName, String password) {
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT NikName, Password FROM User WHERE NikName LIKE " + "'" + nikName + "' AND Password LIKE " + "'" + password + "'", null);
        return cursor.getCount() > 0;
    }

    void insertFriend(String nikName, String friend)
    {
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("NikName", nikName);
        contentValues.put("Friend", friend);
        DB.insert("Friends", null , contentValues);
    }

    ArrayList<String> getFriends(String nikName){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT Friend FROM Friends WHERE NikName LIKE "+ "'" + nikName +"'", null);
        ArrayList<String> friends = new ArrayList<>();
        while (cursor.moveToNext())
        {
            friends.add(cursor.getString(cursor.getColumnIndex("Friend")));
        }
        return friends;
    }

    void removeFriend(String nikName, String friend){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        DB.execSQL("DELETE FROM Friends WHERE NikName LIKE " + "'" + nikName+ "'" + " AND Friend LIKE " + "'" + friend+ "'");
    }

    long insertMessage(String datetime, String myNNFriendsNN, String sender, String recipient, String momeType, String data) {
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("DatetimeAsVarchar", datetime);
        contentValues.put("MyNNFriendsNN", myNNFriendsNN);
        contentValues.put("Sender", sender);
        contentValues.put("Recipient", recipient);
        contentValues.put("MimeType", momeType);
        contentValues.put("Data", data);
        contentValues.put("Datetime", datetime.substring(0,datetime.indexOf('.')).replace('T', ' '));
        contentValues.put("FotoSaved", 0);
        return  DB.insert("Messages", null , contentValues);
    }

    void markMessageSaved(String myNNFriendsNN, String datetimeAsVarchar){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        DB.execSQL("UPDATE Messages SET FotoSaved=1 WHERE MyNNFriendsNN LIKE '"  + myNNFriendsNN+ "' AND DatetimeAsVarchar LIKE '" + datetimeAsVarchar +"'");
        logMessages();

        Log.d("LOG_MyDB", myNNFriendsNN + "   " + datetimeAsVarchar);

    }
    ArrayList<ChatMessage> getMessages(String myNNFriendsNN, String datetime){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT DatetimeAsVarchar, MyNNFriendsNN, Sender, Recipient, MimeType, Data, Datetime, FotoSaved FROM Messages WHERE MyNNFriendsNN LIKE '" +
                myNNFriendsNN + "' AND Datetime > '" + datetime + "'" , null);
        ArrayList<ChatMessage> messages = new ArrayList<>();
        while (cursor.moveToNext())
        {
            messages.add(new ChatMessage(cursor.getString(cursor.getColumnIndex("DatetimeAsVarchar")) , cursor.getString(cursor.getColumnIndex("MyNNFriendsNN")),cursor.getString(cursor.getColumnIndex("Sender")), cursor.getString(cursor.getColumnIndex("Recipient")),
                                    cursor.getString(cursor.getColumnIndex("Datetime")), cursor.getString(cursor.getColumnIndex("MimeType")),
                                    cursor.getString(cursor.getColumnIndex("Data")), cursor.getInt(cursor.getColumnIndex("FotoSaved"))));
        }
        return messages;
    }

    void insertUnreadMessages(String nikName, String friend, String receiveDate)
    {
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("NikName", nikName);
        contentValues.put("Friend", friend);
        contentValues.put("ReceiveDate", receiveDate);
        DB.insert("UnreadMessages", null , contentValues);
    }

    int getCountunreadMessages(String nikname, String friend){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT * FROM UnreadMessages WHERE NikName LIKE '" + nikname  +"' AND Friend LIKE '" + friend +"'", null);
        return cursor.getCount();
    }

    String getMinDateFromUnreadMessages(String nikname, String friend){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT ReceiveDate FROM UnreadMessages WHERE NikName LIKE '" + nikname  +"' AND Friend LIKE '" + friend +"'", null);
        while (cursor.moveToNext())
        {
            String s = cursor.getString(cursor.getColumnIndex("ReceiveDate"));
            removeFromUnreadMessages(nikname, friend);
            return s;
        }
        return null;
    }

    private void removeFromUnreadMessages(String nikName, String friend){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        DB.execSQL("DELETE FROM UnreadMessages WHERE NikName LIKE " + "'" + nikName+ "'" + " AND Friend LIKE " + "'" + friend+ "'");
    }

    void logUser(){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT * FROM User", null);
        while (cursor.moveToNext())
        {
            Log.d("LOG_MyDB", "NikName->" + cursor.getString(cursor.getColumnIndex("NikName")) +
                    " Password->" + cursor.getString(cursor.getColumnIndex("Password")) +
                    " IsLoggedIn->" + cursor.getString(cursor.getColumnIndex("IsLoggedIn"))+"");
        }
    }
    void logFriends(){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT * FROM Friends", null);
        while (cursor.moveToNext())
        {
            Log.d("LOG_MyDB", "NikName ->" + cursor.getString(cursor.getColumnIndex("NikName")) +
                    " Friend->" +cursor.getString(cursor.getColumnIndex("Friend")));
        }
    }

    void logUnreadMessages(){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT * FROM UnreadMessages", null);
        while (cursor.moveToNext())
        {
            Log.d("LOG_MyDB", "NikName ->" + cursor.getString(cursor.getColumnIndex("NikName")) +
                    " Friend->" +cursor.getString(cursor.getColumnIndex("Friend")) +
                    " ReceiveDate->" +cursor.getString(cursor.getColumnIndex("ReceiveDate")));
        }
    }
    void logMessages(){
        SQLiteDatabase DB = myhelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT * FROM Messages", null);
        while (cursor.moveToNext())
        {
            Log.d("LOG_MyDB", "DatetimeAsVarchar ->" + cursor.getString(cursor.getColumnIndex("DatetimeAsVarchar")) +
                    " MyNNFriendsNN ->" + cursor.getString(cursor.getColumnIndex("MyNNFriendsNN")) +
                    " Sender->" + cursor.getString(cursor.getColumnIndex("Sender")) +
                    " Recipient->" + cursor.getString(cursor.getColumnIndex("Recipient")) +
                    " MimeType->" + cursor.getString(cursor.getColumnIndex("MimeType")) +
           //         " Data->" + cursor.getString(cursor.getColumnIndex("Data")) +
                    " Datetime->" +cursor.getString(cursor.getColumnIndex("Datetime"))+
                    " FotoSaved->" +cursor.getString(cursor.getColumnIndex("FotoSaved")));
        }
    }

    static class myDbHelper extends SQLiteOpenHelper
    {
        myDbHelper(Context context) {
            super(context," DB", null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE User (NikName VARCHAR(255) PRIMARY KEY, Password VARCHAR(255), IsLoggedIn INTEGER);");
                db.execSQL("CREATE TABLE Friends (NikName VARCHAR(255), Friend VARCHAR(255), PRIMARY KEY(NikName, Friend));");
                db.execSQL("CREATE TABLE UnreadMessages (NikName VARCHAR(255), Friend VARCHAR(255), ReceiveDate DATETIME)");
                db.execSQL("CREATE TABLE Messages (DatetimeAsVarchar VARCHAR(255), MyNNFriendsNN VARCHAR(255), Sender VARCHAR(255), Recipient VARCHAR(255), " +
                        "MimeType VARCHAR(255), Data VARCHAR, Datetime DATETIME, FotoSaved INTEGER, PRIMARY KEY (DatetimeAsVarchar, MyNNFriendsNN));");
            } catch (Exception ignored) {
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}