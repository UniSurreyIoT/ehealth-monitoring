/**
 * Created by Phil on 16/01/2016.
 * FDialog to select number of samples required
 */
package com.example.phil.bluetoothgatt;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;


public class VisableSampleSelector extends Dialog implements View.OnClickListener {

    NumberPicker samplePicker;
    Button sampleOk,sampleCancel;
    private DialogListener listener;

    public VisableSampleSelector(Context context) {
        super(context);
        setContentView(R.layout.dialog_samplesizepicker);

        samplePicker = (NumberPicker) findViewById(R.id.samplePicker);

        samplePicker.setMaxValue(100);

        sampleOk = (Button) findViewById(R.id.sample_ok_button);
        sampleOk.setOnClickListener(this);

        sampleCancel = (Button) findViewById(R.id.sample_cancel_button);
        sampleCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.sample_ok_button:
                listener.userSelectedAValue(samplePicker.getValue());
                dismiss();
                break;
            case R.id.sample_cancel_button:
                dismiss();
        }
    }

    public static interface DialogListener
    {
        public void userSelectedAValue(int samples);


    }

    public void setCustomEventListener(DialogListener dListener) {
        listener = dListener;
    }
}
