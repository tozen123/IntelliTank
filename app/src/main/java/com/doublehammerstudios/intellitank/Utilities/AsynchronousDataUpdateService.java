package com.doublehammerstudios.intellitank.Utilities;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doublehammerstudios.intellitank.NotificationHelper;
import com.doublehammerstudios.intellitank.PromptDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AsynchronousDataUpdateService extends Service {
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private static final long UPDATE_INTERVAL = 15 * 1000; // 15 seconds
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        startUpdatingValues();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdatingValues();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void startUpdatingValues() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateValues();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
        handler.postDelayed(runnable, UPDATE_INTERVAL);
    }

    private void stopUpdatingValues() {
        handler.removeCallbacks(runnable);
    }

    private void updateValues() {
        getValues("Ammonia Presence Percentage");
        getValues("FEED_LEVEL_PERCENTAGE");
        getValues("Water Temperature");
        getValues("pH Level");
    }

    private void getValues(String node){
        databaseRef.child(node).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(node.equals("Ammonia Presence Percentage") & Float.parseFloat(dataSnapshot.getValue(String.class)) > 50.00){
                    NotificationHelper.createNotification(getApplicationContext(), "IntelliTank: Ammonia Presence Level", "Real-time Monitoring: Ammonia Presence Level is above 50%!.");
                } else if(node.equals("FEED_LEVEL_PERCENTAGE") & Float.parseFloat(dataSnapshot.getValue(String.class)) < 20.00){
                    NotificationHelper.createNotification(getApplicationContext(), "IntelliTank: Feeder System", "Real-time Monitoring: Feed Level is too low please add new feeds to the feeder.");
                } else if(node.equals("Water Temperature") & Float.parseFloat(dataSnapshot.getValue(String.class)) > 35.00){
                    NotificationHelper.createNotification(getApplicationContext(), "IntelliTank: Water Temperature Sensor", "Real-time Monitoring: The tank is feeling too warm, it is above the recommended normal temperature which is 25-30");
                } else if(node.equals("pH Level") & Float.parseFloat(dataSnapshot.getValue(String.class)) < 4.00){
                    NotificationHelper.createNotification(getApplicationContext(), "IntelliTank: pH Level Sensor", "Real-time Monitoring: The tanks pH is too acidic!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createPromptDialog("Firebase Connection Error", "There is an error in connecting to Firebase, please check your Internet");
            }

        });
    }
    private void createPromptDialog(String title, String message){
        PromptDialog dialog = new PromptDialog(getApplicationContext());
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.show();
    }
}
