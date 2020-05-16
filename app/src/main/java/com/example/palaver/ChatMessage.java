package com.example.palaver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class ChatMessage {
    private String sneder;
    private String recipient;
    private String date;
    private String text;
    private String mimetype;

    ChatMessage(String sender, String recipient, Date date, String mimetype, String text){
        this.sneder = sender;
        this.recipient = recipient;
        DateFormat dfOutput = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
        this.date = dfOutput.format(date);
        this.text = text + "\n" + dfOutput.format(date);
        this.mimetype = mimetype;
    }

    ChatMessage(String sender, String recipient, String mimetype, String text){
        this.sneder = sender;
        this.recipient = recipient;
        this.text = text;
        this.mimetype = mimetype;
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

    String getText() {
        return text;
    }

    String getMimetype(){ return mimetype; }
}
