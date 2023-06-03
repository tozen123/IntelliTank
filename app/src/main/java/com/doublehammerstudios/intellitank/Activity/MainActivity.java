package com.doublehammerstudios.intellitank.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.doublehammerstudios.intellitank.Activity.Scan.TankScanActivity;
import com.doublehammerstudios.intellitank.Utilities.AsynchronousDataUpdateService;
import com.doublehammerstudios.intellitank.Fragments.ControlFragment;
import com.doublehammerstudios.intellitank.Fragments.DashboardFragment;
import com.doublehammerstudios.intellitank.Fragments.MonitoringFragment;
import com.doublehammerstudios.intellitank.Fragments.ReportFragment;
import com.doublehammerstudios.intellitank.PromptDialog;
import com.doublehammerstudios.intellitank.R;
import com.doublehammerstudios.intellitank.Utilities.NetworkUtility;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private AlertDialog alertDialog;
    DashboardFragment dashboardFragment = new DashboardFragment();
    MonitoringFragment monitoringFragment = new MonitoringFragment();
    ControlFragment controlFragment = new ControlFragment();
    ReportFragment reportFragment = new ReportFragment();

    MeowBottomNavigation meowBottomNav;
    DatabaseReference notificationsRef;

    private ViewGroup rootView;  // Declare as a class variable
    private Handler handler;
    private Runnable checkInternetRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = getSharedPreferences("deviceSession", Context.MODE_PRIVATE);

        if (sharedPref.contains("deviceSessionID") ) {
            String device_id = sharedPref.getString("deviceSessionID", "");
        } else {
            startActivity(new Intent(this, TankScanActivity.class));
        }

        progressBar = findViewById(R.id.progressBar);

        handler = new Handler();
        checkInternetRunnable = new Runnable() {
            @Override
            public void run() {
                if (NetworkUtility.isNetworkConnected(getApplicationContext())) {
                    hideProgressBar();
                    dismissPromptDialog();
                } else {

                    createPromptDialog("Network Error", "The application cannot connect to the internet. Please check your connection!");
                    showProgressBar();
                    handler.postDelayed(this, 5000);
                }
            }
        };

        handler.post(checkInternetRunnable);


        Intent serviceIntent = new Intent(this, AsynchronousDataUpdateService.class);
        startService(serviceIntent);

        meowBottomNav = findViewById(R.id.MeowBottomNav);
        rootView = findViewById(android.R.id.content);

        meowBottomNav.show(1, true);

        meowBottomNav.add(new MeowBottomNavigation.Model(1, R.drawable.ic_dashboard));
        meowBottomNav.add(new MeowBottomNavigation.Model(2, R.drawable.ic_monitoring));
        meowBottomNav.add(new MeowBottomNavigation.Model(3, R.drawable.ic_reports));
        meowBottomNav.add(new MeowBottomNavigation.Model(4, R.drawable.ic_control));

        notificationsRef = FirebaseDatabase.getInstance().getReference("reports_notif");
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                meowBottomNav.setCount(3, String.valueOf(count));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                createPromptDialog("Firebase Connection Error", "There is an error in connecting to Firebase, please check your Internet");
            }
        });

        meowBottomNav.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()) {
                    case 1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, dashboardFragment).commit();
                        break;
                    case 2:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, monitoringFragment).commit();
                        break;
                    case 3:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, reportFragment).commit();
                        break;
                    case 4:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, controlFragment).commit();
                        break;

                }
                return null;
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, dashboardFragment).commit();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit the IntelliTank?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    private void createPromptDialog(String title, String message){
        if (alertDialog == null || !alertDialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_prompt, null);

            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
            Button dialogOkButton = dialogView.findViewById(R.id.dialogOkButton);

            dialogTitle.setText(title);
            dialogMessage.setText(message);

            dialogOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissPromptDialog();
                }
            });

            builder.setView(dialogView);
            alertDialog = builder.create();
            alertDialog.show();
        }
    }
    private void dismissPromptDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }


    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}