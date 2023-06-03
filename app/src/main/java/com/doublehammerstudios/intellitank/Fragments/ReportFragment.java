package com.doublehammerstudios.intellitank.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.doublehammerstudios.intellitank.Adapters.NotificationAdapter;
import com.doublehammerstudios.intellitank.NotificationHelper;
import com.doublehammerstudios.intellitank.Utilities.NumberPickerDialog;
import com.doublehammerstudios.intellitank.Utilities.ReportCallback;
import com.doublehammerstudios.intellitank.NotificationModel;
import com.doublehammerstudios.intellitank.PromptDialog;
import com.doublehammerstudios.intellitank.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ReportFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private List<NotificationModel> notificationList = new ArrayList<>();
    private Button setLimiterButton;
    private TextView textViewLimitValue;

    int currentLimitValue;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);



        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new NotificationAdapter(notificationList);
        setLimiterButton = view.findViewById(R.id.buttonReportsLimiter);
        textViewLimitValue = view.findViewById(R.id.textViewLimiterValue);

        updateLimiter();

        ReportCallback swipeHandler = new ReportCallback(adapter, getContext());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setAdapter(adapter);

        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("reports_notif");
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                String excessNotificationKey = null;
                for (DataSnapshot notificationSnapshot : dataSnapshot.getChildren()) {
                    NotificationModel notification = notificationSnapshot.getValue(NotificationModel.class);
                    notificationList.add(notification);

                    excessNotificationKey = notificationSnapshot.getKey();
                }

                // Sort the notifications by timestamp in descending order
                Collections.sort(notificationList, new Comparator<NotificationModel>() {
                    @Override
                    public int compare(NotificationModel n1, NotificationModel n2) {
                        return Long.compare(n2.getTimestamp(), n1.getTimestamp());
                    }
                });


                if (notificationList.size() > currentLimitValue) {

                    List<NotificationModel> excessNotifications = notificationList.subList(currentLimitValue, notificationList.size());
                    notificationList.removeAll(excessNotifications);

                    DatabaseReference excessNotificationsRef = notificationsRef.child(excessNotificationKey);
                    excessNotificationsRef.removeValue();
                }

                adapter.notifyDataSetChanged();
                Log.d("ReportFragment", "Number of notifications: " + notificationList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                createPromptDialog("Firebase Connection Error", "There is an error in connecting to Firebase, please check your Internet");
            }
        });


        setLimiterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPicker();
            }
        });

        return view;
    }

    private void createPromptDialog(String title, String message){
        PromptDialog dialog = new PromptDialog(getContext());
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.show();
    }
    private void updateLimiter(){
        databaseRef.child("REPORT_LIMITER").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentLimitValue = dataSnapshot.getValue(Integer.class);
                textViewLimitValue.setText(String.valueOf(currentLimitValue));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                createPromptDialog("Firebase Connection Error", "There is an error in connecting to Firebase, please check your Internet");
            }
        });
    }

    private void openNumberPicker() {
        NumberPickerDialog dialog = new NumberPickerDialog(getContext());
        dialog.setOnConfirmListener(new NumberPickerDialog.OnConfirmListener() {
            @Override
            public void onConfirm(int value) {
                updateLimitValue(value);
            }
        });
        dialog.show();
    }

    private void updateLimitValue(int value) {
        databaseRef.child("REPORT_LIMITER").setValue(value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        currentLimitValue = value;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        createPromptDialog("Firebase Connection Error", "Failed to update the limit value. Please check your internet and try again.");
                    }
                });
    }
}