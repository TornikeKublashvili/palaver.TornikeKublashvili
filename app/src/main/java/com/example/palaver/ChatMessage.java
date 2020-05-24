package com.example.palaver;

class ChatMessage {
    private String sneder;
    private String recipient;
    private String date;
    private String data;
    private String mimetype;
    private int fotoSaved;

    ChatMessage(String sender, String recipient, String date, String mimetype, String data, int fotoSaved){
        this.sneder = sender;
        this.recipient = recipient;
        this.date = date;
        this.data = data;
        this.mimetype = mimetype;
        this.fotoSaved = fotoSaved;
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
}
