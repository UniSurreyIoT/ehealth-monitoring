/**
 * Created by PhilWS in 2016.
 * Dialog view to allow user to select Time
 */

package com.example.phil.bluetoothgatt;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;


public class TimeDialog extends Dialog implements View.OnClickListener{

    NumberPicker hourPicker,minutePicker,secondsPicker;
    Button timeOk,timeCancel;
    private DialogListener listener;

    public TimeDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_temperaturesampleinterval);

            //getActionBar().setTitle(R.string.title_devices);
    //        context = this.getApplicationContext();

            hourPicker = (NumberPicker) findViewById(R.id.hourPicker);
            minutePicker = (NumberPicker) findViewById(R.id.minutePicker);
            secondsPicker = (NumberPicker) findViewById(R.id.secondPicker);

            hourPicker.setMaxValue(24);
            minutePicker.setMaxValue(60);
            secondsPicker.setMaxValue(60);

            timeOk = (Button) findViewById(R.id.time_ok_button);
            timeOk.setOnClickListener(this);

            timeCancel = (Button) findViewById(R.id.time_cancel_button);
            timeCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.time_ok_button:
                listener.userSelectedAValue(hourPicker.getValue(),minutePicker.getValue(),secondsPicker.getValue());
                // listener is object of your MyDialogListener, which you have set from
                // Activity.
                dismiss();
                break;
            case R.id.time_cancel_button:
                dismiss();
        }
    }

    public static interface DialogListener
    {
        public void userSelectedAValue(int hours,int minutes,int seconds);

        public void userCanceled();
    }

    public void setCustomEventListener(DialogListener dListener) {
        listener = dListener;
    }
}
