package com.lukasz.runner.com.lukasz.runner.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lukasz.runner.R;

/**
 * Created by Lukasz on 2017-09-05.
 */

public class InfoDialog {


    //standartowy dialog, po kliknięciu "ok" zamyka dialog
    public static void showOkDialog(Activity activity, String message){
        final Dialog dialog = new Dialog(activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_layout);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.dialogTextView);
        messageTextView.setText(message);
        Button dismissButton = (Button) dialog.findViewById(R.id.dialogButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /*
        dzięki tej metodzie można obsłużyć przycisk "ok" w osobnej klasie, wystarczy zaimplementować interfej OnClick i go tutaj podać
        -----------  PAMIĘTAC ŻEBY ZAMKNĄĆ ZWRÓCONY DIALOG!!!!!  -----------------
     */
    public static Dialog showOkDialog(Activity activity, String message, View.OnClickListener listener){
        final Dialog dialog = new Dialog(activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_layout);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.dialogTextView);
        messageTextView.setText(message);
        Button dismissButton = (Button) dialog.findViewById(R.id.dialogButton);
        if(listener !=null){
            dismissButton.setOnClickListener(listener);
        }
        else{
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }});
        }
        dialog.show();
        return dialog;
    }

    public static Dialog showNoButtonDialog(Activity activity, String message){
        final Dialog dialog = new Dialog(activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_without_button_layout);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.noButtonDialogTextView);
        messageTextView.setText(message);
        dialog.show();
        return dialog;
    }
}
