/**
 * Created by Phil on 16/01/2016.
 * Dialog to select time. Calls a listener on completion
 */

package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
public class SelectTimeDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
    private static int button_id;
    private static int hour;
    private static int minute;
    private static String title;

    public static SelectTimeDialog newInstance(int id,int mHour,int mMinute, String mTitle) {
        title = mTitle;
        hour = mHour;
        minute = mMinute;
        button_id = id;
        return new SelectTimeDialog();

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute,
            DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.setTitle(title);
                // Create a new instance of TimePickerDialog and return it
        return timePickerDialog;
    }



    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mListener.onComplete(hourOfDay,minute,button_id);
    }

public static interface OnCompleteListener {
        public abstract void onComplete(int hour, int minute, int id);
    }

    private OnCompleteListener mListener;

    // make sure the Activity implemented it has a listener
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }
}