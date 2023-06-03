package com.doublehammerstudios.intellitank.Utilities;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.doublehammerstudios.intellitank.Adapters.NotificationAdapter;
import com.doublehammerstudios.intellitank.PromptDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ReportCallback extends ItemTouchHelper.SimpleCallback{
    private NotificationAdapter mAdapter;
    private DatabaseReference mDatabase;
    private Context mContext;
    public ReportCallback(NotificationAdapter adapter, Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mAdapter = adapter;
        mContext = context;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("reports_notif");
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        long notificationTimestamp = mAdapter.getNotificationTimestamp(position);


        Query query = mDatabase.orderByChild("timestamp").equalTo(notificationTimestamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
                Toast.makeText(mContext, "Report has been succesfully deleted!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                createPromptDialog("Firebase Connection Error", "There is an error in updating values to Firebase, please check your Internet");
            }
        });
        mAdapter.notifyItemRemoved(position);
    }

    public void createPromptDialog(String message, String title){
        PromptDialog dialog = new PromptDialog(mContext);
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.show();
    }
}
