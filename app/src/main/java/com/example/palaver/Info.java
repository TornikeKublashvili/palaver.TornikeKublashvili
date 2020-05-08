package com.example.palaver;

import android.content.Context;
import android.net.ConnectivityManager;
import android.view.Gravity;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

class Info {

    public enum Color{Red, Green}

    static void show(Context context, String text, Color color){
        android.widget.Toast toast= android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 75);
        TextView v = toast.getView().findViewById(android.R.id.message);
        if(color == Color.Red){
            v.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
        else if(color == Color.Green){
            v.setTextColor(ContextCompat.getColor(context, R.color.green));
        }
        toast.show();
    }

    static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
