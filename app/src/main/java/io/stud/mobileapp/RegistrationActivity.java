package io.stud.mobileapp;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Author : Hutinet Maxime & Nagy Livio
 */

public class RegistrationActivity extends AppCompatActivity {

    TextView androidUserID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        androidUserID = (TextView) findViewById(R.id.androidUserIDText);

        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        androidUserID.setText("Please go to the administration to register !\n\nYour ID : " + androidId);
    }




}
