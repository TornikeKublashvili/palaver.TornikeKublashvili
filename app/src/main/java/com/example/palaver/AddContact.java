package com.example.palaver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

public class AddContact extends DialogFragment {

    private Activity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.add_contact, null);

        builder.setView(view)
            .setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                if (Info.isNetworkAvailable(activity)) {
                    try{
                        EditText editText  = view.findViewById(R.id.add_contact_dialog_edit_text);
                        String friendsNikName = editText.getText().toString();

                        JSONObject json = new JSONObject();
                        json.put("Username", MainActivity.nikName);
                        json.put("Password",  MainActivity.password);
                        json.put("Friend", friendsNikName);

                        JSONObject response = new NetworkHelper().execute("api/friends/add", json.toString()).get();

                        if(response.getInt("MsgType")==0){
                            Info.show(activity, response.getString("Info"), Info.Color.Red);
                        }
                        else if(response.getInt("MsgType")==1){
                            MainActivity.DB.insertFriend(friendsNikName);
                            Info.show(activity, response.getString("Info"), Info.Color.Green);
                            ((ContactList)getActivity()).onResume();                            }
                    }
                    catch(Exception e){
                        Log.d("LOG_AddContact", e.toString());
                    }
                }else {
                    Info.show(activity, getString(R.string.error_while_login), Info.Color.Red);
                    Log.d("LOG_AddContact", getString(R.string.noInternetConnection));
                }
                }
            })
            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AddContact.this.getDialog().dismiss();
                }
            });

        builder.setMessage(getString(R.string.add_coontact));
        return builder.create();
    }
}
