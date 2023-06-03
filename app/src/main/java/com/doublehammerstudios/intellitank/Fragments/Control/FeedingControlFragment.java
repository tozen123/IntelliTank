package com.doublehammerstudios.intellitank.Fragments.Control;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


public class FeedingControlFragment extends Fragment {
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    LinearLayout containerLayout;
    Button buttonConfirm;

    private ChipGroup chipGroupSelectDay;
    int occurence;
    EditText numberEditText;
    String feederDatabaseConfiguration;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feeding_control, container, false);
        retrieveDatabaseConfiguration("FEEDING_SYSTEM_CONFIGURATION");

        numberEditText = view.findViewById(R.id.occurenceEditTextBox);
        containerLayout = view.findViewById(R.id.containerLayout);
        chipGroupSelectDay = view.findViewById(R.id.chipGroupSelectDay);

        buttonConfirm = view.findViewById(R.id.confirmFilteringSystemConfig);
        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String inputText = s.toString().trim();
                int number = 0;

                if (!inputText.isEmpty()) {
                    number = Integer.parseInt(inputText);
                }
                occurence = number;
                updateEditTextFields(number);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> checkedChipIds = chipGroupSelectDay.getCheckedChipIds();
                List<String> editTextDataList = getEditTextData();

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
                messageTextView.setText("Are you sure with the configuration? \n\nSelected Days: " + getSelectedDays(checkedChipIds) + "\n\nOccurence per day: "+occurence
                                        + "\n\nOccurences:[ "+editTextDataList+" ]");

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean isFormatValid = true;

                        for (int i = 0; i < editTextDataList.size(); i++) {
                            String timeString = editTextDataList.get(i).trim();
                            if (!isValidTimeFormat(timeString)) {
                                isFormatValid = false;
                                editTextDataList.set(i, "");
                                createPromptDialog("Invalid input at the time occurence at (" + timeString +") please use 24 hour format (14:30) .", "Invalid Input");
                            }
                        }

                        if (isFormatValid) {
                            updateConfiguration(getSelectedDays(checkedChipIds), occurence, editTextDataList);
                            createPromptDialog("You have successfully update your filter configuration!", "Success");
                        }

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
        });
        return view;
    }
    private void retrieveDatabaseConfiguration(String node){
        databaseRef.child(node).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                feederDatabaseConfiguration = dataSnapshot.getValue(String.class);
                changeCurrentFeederConfiguration(feederDatabaseConfiguration);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "failed retrieved configuration!", Toast.LENGTH_SHORT).show();
                createPromptDialog("Firebase Connection Error", "There is an error in connecting to Firebase, please check your Internet");
            }
        });
    }

    private void changeCurrentFeederConfiguration(String feederDatabaseConfiguration){
        int openingBracketIndex = feederDatabaseConfiguration.indexOf("[");
        int closingBracketIndex = feederDatabaseConfiguration.indexOf("]");
        String daysString = feederDatabaseConfiguration.substring(openingBracketIndex + 1, closingBracketIndex);

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

        String[] parts = feederDatabaseConfiguration.split("\\|");
        String occurrenceString = parts[1].trim();
        numberEditText.setText(occurrenceString);

        String timePortion = parts[2].trim();
        String[] timeValues = timePortion.substring(1, timePortion.length() - 1).split(", ");
        for (int i = 0; i < timeValues.length; i++) {
            String timeValue = timeValues[i];

            // Check if the index is within the bounds of the containerLayout
            if (i < containerLayout.getChildCount()) {
                View childView = containerLayout.getChildAt(i);
                if (childView instanceof EditText) {
                    EditText editText = (EditText) childView;
                    editText.setText(timeValue);
                }
            }
        }

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
    private void updateEditTextFields(int number) {
        containerLayout.removeAllViews();

        for (int i = 0; i < number; i++) {
            EditText editText = new EditText(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            editText.setPadding(25,30,25,30);

            params.setMargins(0, 45, 0, 45);

            editText.setInputType(EditorInfo.TYPE_CLASS_DATETIME);
            editText.setTextSize(11);
            editText.setBackground(getResources().getDrawable(R.drawable.rounded_edittext_background));

            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showTimePickerDialog(editText);
                }
            });

            editText.setHint("Click this hint or type here the time for occurence: " + (i + 1));
            containerLayout.addView(editText);
        }
    }

    private boolean isValidTimeFormat(String timeString) {
        String timeRegex = "^([01]\\d|2[0-3]):([0-5]\\d)$";
        Pattern pattern = Pattern.compile(timeRegex);
        return pattern.matcher(timeString).matches();
    }

    private void showTimePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                editText.setText(selectedTime);
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private List<String> getEditTextData() {
        List<String> editTextDataList = new ArrayList<>();

        int childCount = containerLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = containerLayout.getChildAt(i);
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                String editTextData = editText.getText().toString().trim();
                editTextDataList.add(editTextData);
            }
        }

        return editTextDataList;
    }
    private void updateConfiguration(List<String> a, int b,  List<String> editTextDataList){
        String config = String.valueOf(a) + " | " + b + " | " + String.valueOf(editTextDataList);
        databaseRef.child("FEEDING_SYSTEM_CONFIGURATION").setValue(config)
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

    public void createPromptDialog(String message, String title){
        PromptDialog dialog = new PromptDialog(getContext());
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.show();
    }
}