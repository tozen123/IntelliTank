package com.doublehammerstudios.intellitank.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.doublehammerstudios.intellitank.Activity.Scan.TankScanActivity;
import com.doublehammerstudios.intellitank.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button buttonEndSession = findViewById(R.id.buttonEndSession);

        buttonEndSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences("deviceSession", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();

                startActivity(new Intent(SettingsActivity.this, TankScanActivity.class));
                finish();
            }
        });
    }
}