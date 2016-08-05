/**
 * Created by Phil on 16/01/2016.
 * Dialog to select a date value.
 * Calls a Listener on closure
 */

package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

public class SelectDateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    private static String title;
    private static int button_id;
    private static int year,month,day;
    private OnCompleteListener mListener;

    //Constructor
    public static SelectDateDialog newInstance(int id,int mYear,int mMonth,int mDay, String mTitle) {
        title = mTitle;
        button_id = id;
        year = mYear;
        month = mMonth;
        day = mDay;
        return new SelectDateDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this,year,month,day);
        datePickerDialog.setTitle(title);
        // Create a new instance of TimePickerDialog and return it
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mListener.onComplete(dayOfMonth,monthOfYear,year,button_id);
    }

    public static interface OnCompleteListener {
        public abstract void onComplete(int day,int month,int year, int id);
    }

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