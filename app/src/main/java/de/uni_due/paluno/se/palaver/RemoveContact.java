package de.uni_due.paluno.se.palaver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.RequiresApi;
import org.json.JSONObject;

import java.util.Objects;

public  class RemoveContact extends DialogFragment {

    private Activity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.remove_contact, null);

        builder.setView(view)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeFriend(getArguments().getString("contact"));
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RemoveContact.this.getDialog().dismiss();
                    }
                });
        String message = getString(R.string.do_you_want_to_delete_contact);
        if(message.contains("xxx")){
            message = message.replace("xxx", Objects.requireNonNull(getArguments().getString("contact")));
        }
        builder.setMessage(message);
        return builder.create();
    }

    public void removeFriend(String friend){
        if (Info.isNetworkAvailable(activity)) {
            try {
                JSONObject json = new JSONObject();
                json.put("Username", MainActivity.nikName);
                json.put("Password", MainActivity.password);
                json.put("Friend", friend);

                JSONObject response = new NetworkHelper().execute("api/friends/remove", json.toString()).get();

                if (response.getInt("MsgType") == 0) {
                    Info.show(activity, response.getString("Info"), Info.Color.Red);
                } else if (response.getInt("MsgType") == 1) {
                    MainActivity.DB.removeFriend(MainActivity.nikName, friend);
                    Info.show(activity, response.getString("Info"), Info.Color.Green);
                    ((ContactList)getActivity()).onResume();
                }
            }
            catch(Exception e){
                Log.d("LOG_RemoveContact", e.toString());
            }
        }else {
            Log.d("LOG_RemoveContact", getString(R.string.noInternetConnection));
        }
    }
}
