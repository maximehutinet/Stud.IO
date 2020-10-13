package io.stud.mobileapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Author : Hutinet Maxime & Nagy Livio
 */

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }
}




