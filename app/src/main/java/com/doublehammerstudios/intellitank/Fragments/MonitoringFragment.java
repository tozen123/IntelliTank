package com.doublehammerstudios.intellitank.Fragments;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.doublehammerstudios.intellitank.NotificationHelper;
import com.doublehammerstudios.intellitank.PromptDialog;
import com.doublehammerstudios.intellitank.R;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MonitoringFragment extends Fragment {


    public TextView ammoniaValue, phValue, wTempValue, feederValue;

    String ammoniaVal, phVal, wTempVal, feederVal;

    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    private Handler handler;
    private Runnable runnable;

    private LinearLayout ammoniaLL;
    private LinearLayout waterTempLL;
    private LinearLayout phLevelLL;
    private LinearLayout feederLL;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitoring, container, false);

        ammoniaValue = view.findViewById(R.id.headerAmmoniaValue);
        phValue = view.findViewById(R.id.headerPhLevelValue);
        wTempValue = view.findViewById(R.id.headerWaterTempValue);
        feederValue = view.findViewById(R.id.headerFeedLevelValue);

        ammoniaLL = view.findViewById(R.id.headerAmmonia);
        waterTempLL = view.findViewById(R.id.headerWaterTemp);
        phLevelLL = view.findViewById(R.id.headerPhLevel);
        feederLL = view.findViewById(R.id.headerFeedLevel);

        ammoniaLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ammoniaClick();
            }
        });

        waterTempLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waterTemperatureClick();
            }
        });

        phLevelLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pHLevelClick();
            }
        });

        feederLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feederStorageLevelClick();
            }
        });

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                updateValues();
                handler.postDelayed(this, 15000);
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startUpdatingValues();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUpdatingValues();
    }

    private void startUpdatingValues() {
        handler.postDelayed(runnable, 0);
    }

    private void stopUpdatingValues() {
        handler.removeCallbacks(runnable);
    }

    private void updateValues() {
        getValues("Ammonia Presence Percentage", ammoniaValue);
        getValues("FEED_LEVEL_PERCENTAGE", feederValue);
        getValues("Water Temperature", wTempValue);
        getValues("pH Level", phValue);
    }
    private void getValues(String node, TextView textViewValue){
        databaseRef.child(node).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textViewValue.setText(dataSnapshot.getValue(String.class));

                if(node.equals("Ammonia Presence Percentage") & Float.parseFloat(dataSnapshot.getValue(String.class)) > 50.00){
                    NotificationHelper.createNotification(getContext(), "IntelliTank: Ammonia Presence Level", "Real-time Monitoring: Ammonia Presence Level is above 50%!.");
                } else if(node.equals("FEED_LEVEL_PERCENTAGE") & Float.parseFloat(dataSnapshot.getValue(String.class)) < 20.00){
                    NotificationHelper.createNotification(getContext(), "IntelliTank: Feeder System", "Real-time Monitoring: Feed Level is too low please add new feeds to the feeder.");
                } else if(node.equals("Water Temperature") & Float.parseFloat(dataSnapshot.getValue(String.class)) > 35.00){
                    NotificationHelper.createNotification(getContext(), "IntelliTank: Water Temperature Sensor", "Real-time Monitoring: The tank is feeling too warm, it is above the recommended normal temperature which is 25-30");
                } else if(node.equals("pH Level") & Float.parseFloat(dataSnapshot.getValue(String.class)) < 5.50){
                    NotificationHelper.createNotification(getContext(), "IntelliTank: pH Level Sensor", "Real-time Monitoring: The tanks pH is too acidic!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                createPromptDialog("Firebase Connection Error", "There is an error in connecting to Firebase, please check your Internet");
            }
        });
    }

    private void createPromptDialog(String title, String message){
        PromptDialog dialog = new PromptDialog(getContext());
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.show();
    }

    public void ammoniaClick(){
        float ammonia = Float.parseFloat(String.valueOf(ammoniaValue.getText()));
        String message;
        if(ammonia > 50.00){
            message = "The presence of ammonia in the tank is high. (" + ammonia + ") This might be caused by Overfeeding, overcrowding, poor tank maintenance, and organic decay of aquarium plant.";
        } else {
            message = "Ammonia level is in normal value. Avoid overfeeding and overcrowding your tank to keep this value. ("+ ammonia + ")";
        }
        createPromptDialog("Ammonia Presence", message);
    }
    public void waterTemperatureClick(){
        float temp = Float.parseFloat(String.valueOf(wTempValue.getText()));
        String message;
        if(temp > 35.00){
            message = "The water temperature in the tank is high. (" + temp + " C ) This might be caused by lack of air conditioning and summer season";
        } else {
            message = "Water temperature is in ideal range, please keep the temperature in 24-32 degree celsius. ("+ temp + " C )";
        }
        createPromptDialog("Water temperature", message);
    }
    public void pHLevelClick(){
        float ph = Float.parseFloat(String.valueOf(phValue.getText()));
        String message;
        if(ph < 5.50){
            message = "The pH (Potential of Hydrogen) Level of the tank is low or acidic. (" + ph + ") This might be poor maintenance.";
        } else {
            message = "The pH (Potential of Hydrogen) Level is in ideal range, please keep it around 6-8. (" + ph + ")";
        }
        createPromptDialog("pH (Potential of Hydrogen) Level", message);
    }
    public void feederStorageLevelClick(){
        float feed = Float.parseFloat(String.valueOf(feederValue.getText()));
        String message;
        if(feed < 20.00){
            message = "The automatic feeder storage level is low, (" + feed + "). Please add feeds to avoid fish dying of hunger :(";
        } else {
            message = "The automatic feeder storage level is in ideal range, please keep it greater than 20 percent. (" + feed + ")";
        }
        createPromptDialog("Automatic Feeder Storage Level", message);
    }

}