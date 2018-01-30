package com.lukasz.runner.com.lukasz.runner.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lukasz.runner.R;
import com.lukasz.runner.activities.MapsActivity;

import java.util.Map;

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

    /*
        Pokazuje dialog z przycieskiem anuluj. Do przycisków dodatkowo używana jest metoda setTag() do przekazania refrencji do dialogu
        bo nie potrafiłem sobie go wyvigąnąć w OnClickListener. Żeby działać na dialogu trzeba zrobić:
        Dialog dialog = (Dialog) v.getTag();
     */
    public static void showCancelDialog(Activity activity, String message, View.OnClickListener listener){
        Dialog dialog = new Dialog(activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_with_cancel_button_layout);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.cancelDialogTextView);
        messageTextView.setText(message);
        Button okButton = (Button) dialog.findViewById(R.id.cancelDialogOkButton);
        Button cancelButton = (Button) dialog.findViewById(R.id.cancelDialogCancelButton);
        okButton.setOnClickListener(listener);
        okButton.setTag(dialog);
        cancelButton.setOnClickListener(listener);
        cancelButton.setTag(dialog);
        dialog.show();
    }


    public static void showTrackTimesDialog(MapsActivity mapsActivity,  Map<String, String> trackTimes){
        AlertDialog.Builder builder = new AlertDialog.Builder(mapsActivity);
        LayoutInflater inflater = mapsActivity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.track_times_dialog_layout, null));
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        ImageView exit = (ImageView) dialog.findViewById(R.id.trackTimesDialogExitButton);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView[] trackTimesTextArray = new TextView[5];
        trackTimesTextArray[0] = (TextView) dialog.findViewById(R.id.firstTrackTimeText);
        trackTimesTextArray[1] = (TextView) dialog.findViewById(R.id.secondTrackTimeText);
        trackTimesTextArray[2] = (TextView) dialog.findViewById(R.id.thirdTrackTimeText);
        trackTimesTextArray[3] = (TextView) dialog.findViewById(R.id.fourthTrackTimeText);
        trackTimesTextArray[4] = (TextView) dialog.findViewById(R.id.fifthTrackTimeText);
        String[] keys =  trackTimes.keySet().toArray(new String[trackTimes.size()]);
        for(int i=0; i<trackTimes.size(); i++){
            trackTimesTextArray[i].setOnClickListener(mapsActivity);
            trackTimesTextArray[i].setText(trackTimes.get(keys[i]));
            trackTimesTextArray[i].setHint(keys[i]);
        }
    }
}
