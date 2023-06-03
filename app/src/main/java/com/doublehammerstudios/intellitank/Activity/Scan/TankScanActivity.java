package com.doublehammerstudios.intellitank.Activity.Scan;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.doublehammerstudios.intellitank.GlobalConfiguration;
import com.doublehammerstudios.intellitank.Activity.MainActivity;
import com.doublehammerstudios.intellitank.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class TankScanActivity extends AppCompatActivity {

    Button buttonScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tank_scan);

        SharedPreferences sharedPref = getSharedPreferences("deviceSession", Context.MODE_PRIVATE);

        if (sharedPref.contains("deviceSessionID") ) {
            startActivity(new Intent(this, MainActivity.class));
        }

        buttonScan = findViewById(R.id.scanButton);

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(TankScanActivity.this);
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.setPrompt("Scan your IntelliTank QR Code");
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                intentIntegrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences sharedPref = getSharedPreferences("deviceSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(intentResult!=null){
            String content  = intentResult.getContents();

            if(content == null){
                Toast.makeText(this, "Failed to analyze the content. It seems to be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            String device_id = content.split(":")[1];

            if(GlobalConfiguration.confirmDevice(device_id)){
                Toast.makeText(this, "Device has been successfully scanned!", Toast.LENGTH_SHORT).show();
                editor.putString("deviceSessionID", device_id);
                editor.apply();

                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "QR Code is not valid!", Toast.LENGTH_SHORT).show();
            }

        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onBackPressed() {
        // Display a dialog or toast message asking for confirmation
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit the IntelliTank?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        TankScanActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}