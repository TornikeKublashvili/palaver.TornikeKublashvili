package com.example.palaver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatMessage>{

    private List<ChatMessage> chatMessages;
    private Context context;

    ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        super(context, R.layout.activity_chat);
        this.chatMessages = chatMessages;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        ChatMessage message = chatMessages.get(position);
        View rowView;

        if(message.getSender().equals(MainActivity.sharedPreferences.getString("NikName",""))){
            rowView = inflater.inflate(R.layout.my_message, parent, false);
        }
        else{
            rowView = inflater.inflate(R.layout.friends_message, parent, false);
        }
        TextView dateTextView =  rowView.findViewById(R.id.TextView_Message);
        dateTextView.setText(message.getText());
        return rowView;
    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public ChatMessage getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public void add(ChatMessage object) {
        chatMessages.add(object);
    }
}
