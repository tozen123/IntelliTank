package com.doublehammerstudios.intellitank;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class PromptDialog extends Dialog {

    private TextView dialogTitle;
    private TextView dialogMessage;
    private Button dialogOkButton;
    public PromptDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_prompt);

        dialogTitle = findViewById(R.id.dialogTitle);
        dialogMessage = findViewById(R.id.dialogMessage);
        dialogOkButton = findViewById(R.id.dialogOkButton);

        dialogOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    public void setMessage(String message) {
        dialogMessage.setText(message);
    }
    public void setTitle(String title) {
        dialogTitle.setText(title);
    }
}