package de.uni_due.paluno.se.palaver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;


public class Version extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);
        String versionName = getString(R.string.AppVersion) + ": " + BuildConfig.VERSION_NAME;
        TextView textviev_AppVersion = findViewById(R.id.TextView_AppVersion);
        textviev_AppVersion.setText(versionName);
    }
}
