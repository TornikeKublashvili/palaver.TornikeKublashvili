package de.uni_due.paluno.se.palaver;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    public static MyDB DB;
    public static String nikName ;
    public static String password;
    public static String chatPartner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent tokenService = new Intent(MainActivity.this, TokenService.class);
        startService(tokenService);

        DB = new MyDB(this);

        getLoggesUser();

        if (nikName != null){
            Intent intent = new Intent(MainActivity.this, ContactList.class);
            startActivity(intent);
            finish();
        }
        else{
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
    }

    private void getLoggesUser(){
        String[] s = DB.getLoggedUser();
        nikName = s[0];
        password = s[1];
    }
}