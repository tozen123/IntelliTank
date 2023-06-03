package com.doublehammerstudios.intellitank.Fragments.Control;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.doublehammerstudios.intellitank.PromptDialog;
import com.doublehammerstudios.intellitank.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;


public class MainControlSystemFragment extends Fragment {

    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    public String aeratorStatus, filteringStatus, feedingStatus;

    public Button buttonFilteringSetStatus, buttonFeedingSetStatus;
    public TextView textViewFilteringStatusValue, textViewFeedingStatusValue;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_control_system, container, false);

        textViewFilteringStatusValue = view.findViewById(R.id.statusValueFilter);
        textViewFeedingStatusValue = view.findViewById(R.id.statusValueFeeder);

        buttonFilteringSetStatus = view.findViewById(R.id.statusSetFilter);
        buttonFeedingSetStatus = view.findViewById(R.id.statusSetFeeder);

        getValues("FEEDER_STATUS", textViewFeedingStatusValue);
        getValues("FILTER_STATUS", textViewFilteringStatusValue);

        buttonFeedingSetStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textViewFeedingStatusValue.getText().toString().equals("ON")){
                    buttonFeedingSetStatus.setText("RUN FORCE FEEDING");
                    updateValue("FEEDER_STATUS", "OFF");
                } else {
                    buttonFeedingSetStatus.setText("STOP FORCE FEEDING");
                    updateValue("FEEDER_STATUS", "ON");
                }
            }
        });

        buttonFilteringSetStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textViewFilteringStatusValue.getText().toString().equals("ON")){
                    buttonFilteringSetStatus.setText("RUN FORCE FILTERING");
                    updateValue("FILTER_STATUS", "OFF");
                } else {
                    buttonFilteringSetStatus.setText("STOP FORCE FILTERING");
                    updateValue("FILTER_STATUS", "ON");
                }
            }
        });

        return view;
    }

    private void getValues(String node, TextView textViewValue){
        databaseRef.child(node).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textViewValue.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                createPromptDialog("Firebase Connection Error", "There is an error in connecting to Firebase, please check your Internet");
            }
        });
    }

    private void updateValue(String node, String newValue) {
        databaseRef.child(node).setValue(newValue)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Value updated successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        createPromptDialog("Firebase Connection Error", "There is an error in updating values to Firebase, please check your Internet");
                    }
                });
    }

    private void createPromptDialog(String title, String message){
        PromptDialog dialog = new PromptDialog(getContext());
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.show();
    }
}