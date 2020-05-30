package de.uni_due.paluno.se.palaver;

import android.database.sqlite.SQLiteDatabase;

class ChatMessage {
    private String datetimeAsVarchar;
    private String myNNFriendsNN;
    private String sneder;
    private String recipient;
    private String date;
    private String data;
    private String mimetype;
    private int fotoSaved;

    ChatMessage(String datetimeAsVarchar, String myNNFriendsNN, String sender, String recipient, String date, String mimetype, String data, int fotoSaved){
        this.datetimeAsVarchar = datetimeAsVarchar;
        this.sneder = sender;
        this.recipient = recipient;
        this.date = date;
        this.data = data;
        this.mimetype = mimetype;
        this.fotoSaved = fotoSaved;
        this.myNNFriendsNN = myNNFriendsNN;
    }

    String getDatetimeAsVarchar() {
        return datetimeAsVarchar;
    }

    String getMyNNFriendsNN() {
        return myNNFriendsNN;
    }

    String getSender() {
        return sneder;
    }

    String getRecipient() {
        return recipient;
    }

    String getDate() {
        return date;
    }

    String getData() {
        return data;
    }

    String getMimetype(){ return mimetype; }

    int getFotoSaved(){ return  fotoSaved; }

    void setFotoSaved(){
        fotoSaved = 1;
    }
}
