package de.uni_due.paluno.se.palaver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

        if(message.getSender().equals(MainActivity.nikName)){
            rowView = inflater.inflate(R.layout.my_message, parent, false);
        }
        else{
            rowView = inflater.inflate(R.layout.friends_message, parent, false);
        }
        final TextView textViewMessage =  rowView.findViewById(R.id.TextView_Message);
        final ImageView imageViewMessage  = rowView.findViewById(R.id.ImageView_Message);
        final TextView textViewMessageDownload =  rowView.findViewById(R.id.TextView_Message_Download);

        switch (message.getMimetype()) {
            case "text/plain":
                textViewMessage.setText(message.getData() + "\n" + message.getDate());
                break;
            case "location/plain":
                //TODO hardcoded String [Location von] must be defined in strings.xml
                textViewMessage.setText("Location von " + message.getSender() + "\n" + message.getDate());
                break;
            case "Image/*":
                textViewMessage.setText(message.getDate());
                imageViewMessage.setImageBitmap(Methods.base64ToBitmap(message.getData()));
                imageViewMessage.setVisibility(View.VISIBLE);

                if(message.getFotoSaved() == 0){
                    textViewMessageDownload.setVisibility(View.VISIBLE);
                }
                else{
                    textViewMessageDownload.setVisibility(View.GONE);
                    imageViewMessage.setAlpha(1.0F);
                }
                break;
            case "Video/*":
                //TODO hardcoded String [Video von] must be defined in strings.xml
                textViewMessage.setText("Video von " + message.getSender() + "\n" + message.getDate());
                break;
            default:
                textViewMessage.setText(message.getMimetype() + "\n" + message.getDate());
                break;
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
