package com.doublehammerstudios.intellitank.Utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;

import com.doublehammerstudios.intellitank.R;

public class NumberPickerDialog {
    private Context context;
    private AlertDialog alertDialog;
    private NumberPicker numberPicker;

    public NumberPickerDialog(Context context) {
        this.context = context;
        initDialog();
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_number_picker, null);
        numberPicker = view.findViewById(R.id.numberPicker);

        // Customize the numberPicker as needed
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);

        builder.setView(view)
                .setTitle("Set Limit")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int value = numberPicker.getValue();
                        if (onConfirmListener != null) {
                            onConfirmListener.onConfirm(value);
                        }
                    }
                })
                .setNegativeButton("Cancel", null);

        alertDialog = builder.create();
    }

    public void show() {
        alertDialog.show();
    }

    private OnConfirmListener onConfirmListener;

    public interface OnConfirmListener {
        void onConfirm(int value);
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.onConfirmListener = listener;
    }
}

