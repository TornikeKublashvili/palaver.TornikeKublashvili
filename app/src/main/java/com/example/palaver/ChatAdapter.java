package com.example.palaver;

import android.annotation.SuppressLint;
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

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        final ChatMessage message = chatMessages.get(position);
        View rowView;

        if(message.getSender().equals(MainActivity.sharedPreferences.getString("NikName",""))){
            rowView = inflater.inflate(R.layout.my_message, parent, false);
        }
        else{
            rowView = inflater.inflate(R.layout.friends_message, parent, false);
        }
        final TextView dateTextView =  rowView.findViewById(R.id.TextView_Message);
        if(message.getMimetype().equals("text/plain")){
            dateTextView.setText(message.getText());
        }
        else if(message.getMimetype().equals("location/plain")){
            //TODO hardcoded String [Location von] must be defined in strings.xml
            dateTextView.setText("Location von"+ message.getSender() + "\n" + message.getDate());
        }
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
