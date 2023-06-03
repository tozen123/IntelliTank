package com.doublehammerstudios.intellitank.Fragments.Control;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.doublehammerstudios.intellitank.PromptDialog;
import com.doublehammerstudios.intellitank.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FilteringControlFragment extends Fragment {
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private Button buttonConfirm;

    private TimePicker timePicker;
    private ChipGroup chipGroupSelectDay;
    String selectedTime;
    String filterDatabaseConfiguration;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtering_control, container, false);
        retrieveDatabaseConfiguration("FILTERING_SYSTEM_CONFIGURATION");

        buttonConfirm = view.findViewById(R.id.confirmFilteringSystemConfig);

        timePicker = view.findViewById(R.id.filteringTimePicker);
        chipGroupSelectDay = view.findViewById(R.id.chipGroupSelectDay);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmConfiguration();
            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

                selectedTime = hourOfDay + ":" + minute;
            }
        });

        return view;
    }
    private void retrieveDatabaseConfiguration(String node){
        databaseRef.child(node).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                filterDatabaseConfiguration = dataSnapshot.getValue(String.class);
                changeCurrentFilterConfiguration(filterDatabaseConfiguration);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "failed retrieved configuration!", Toast.LENGTH_SHORT).show();
                createPromptDialog("Firebase Connection Error", "There is an error in connecting to Firebase, please check your Internet");
            }
        });
    }
    public void changeCurrentFilterConfiguration(String filterDatabaseConfiguration){

        int openingBracketIndex = filterDatabaseConfiguration.indexOf("[");
        int closingBracketIndex = filterDatabaseConfiguration.indexOf("]");
        String daysString = filterDatabaseConfiguration.substring(openingBracketIndex + 1, closingBracketIndex);

        String[] daysArray = daysString.split(", ");

        for (int i = 0; i < chipGroupSelectDay.getChildCount(); i++) {
            View childView = chipGroupSelectDay.getChildAt(i);
            if (childView instanceof Chip) {
                Chip chip = (Chip) childView;
                String chipText = chip.getText().toString().trim();
                boolean isChipSelected = false;

                for (String day : daysArray) {
                    if (chipText.equalsIgnoreCase(day)) {
                        chip.setChecked(true);
                        isChipSelected = true;
                        break;
                    }
                }

                if (!isChipSelected) {
                    chip.setChecked(false);
                }
            }
        }

        int timeSeparatorIndex = filterDatabaseConfiguration.indexOf("|");
        String timeString = filterDatabaseConfiguration.substring(timeSeparatorIndex + 1).trim();
        String[] timeParts = timeString.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        timePicker.setHour(hour);
        timePicker.setMinute(minute);

        Toast.makeText(getActivity(), "Successfully retrieved configuration!", Toast.LENGTH_SHORT).show();
    }
    public void confirmConfiguration(){
        List<Integer> checkedChipIds = chipGroupSelectDay.getCheckedChipIds();

        if(checkedChipIds.size() == 0){
            createPromptDialog("Incomplete Configuration: Please select a day.", "Hold on!");
            return;
        }

        createAlertDialog(checkedChipIds);
    }

    private List<String> getSelectedDays(List<Integer> checkedChipIds){
        List<String> selectedDays = new ArrayList<String>();
        for (int chipId : checkedChipIds) {
            Chip chip = chipGroupSelectDay.findViewById(chipId);
            if (chip != null) {
                String chipText = chip.getText().toString();
                selectedDays.add(chipText);
            }
        }
        return selectedDays;
    }

    public void createPromptDialog(String message, String title){
        PromptDialog dialog = new PromptDialog(getContext());
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.show();
    }

    private void createAlertDialog(List<Integer> checkedChipIds){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_prompt, null);
        AlertDialog dialog = builder.setView(dialogView).create();

        TextView titleTextView = dialogView.findViewById(R.id.dialogTitle);
        TextView messageTextView = dialogView.findViewById(R.id.dialogMessage);
        Button okButton = dialogView.findViewById(R.id.dialogOkButton);
        Button cancelButton = dialogView.findViewById(R.id.dialogCancelButton);

        cancelButton.setEnabled(true);

        titleTextView.setText("New Config Confirmation");
        messageTextView.setText("Are you sure with the configuration? \n\nSelected Days: " + getSelectedDays(checkedChipIds) + "\n\nSelected Time Per Day: "+selectedTime);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateConfiguration(getSelectedDays(checkedChipIds), selectedTime);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void updateConfiguration(List<String> a, String b){
        String config = String.valueOf(a) + " | " + b;
        databaseRef.child("FILTERING_SYSTEM_CONFIGURATION").setValue(config)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        createPromptDialog("Firebase Connection Error", "There is an error in updating values to Firebase, please check your Internet");
                    }
                });
    }
}