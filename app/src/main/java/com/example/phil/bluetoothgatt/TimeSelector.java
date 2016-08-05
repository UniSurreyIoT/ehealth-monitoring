/**
 * Created by PWS 2016
 * Time Selector for HealthMonitoring
 * Used to create a number picker to select the length of time to collect samples
 */

package com.example.phil.bluetoothgatt;



import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class TimeSelector extends DialogPreference {
    private long temperatureSample;
    //Set up defaults
    public static final int HOUR_MAX_VALUE = 24;
    public static final int HOUR_MIN_VALUE = 0;
    public static final int MINUTE_MAX_VALUE = 59;
    public static final int MINUTE_MIN_VALUE = 0;
    public static final int TIME_DEFAULT = 5;

    private static final String androidns = "http://schemas.android.com/apk/res/android";

    private Context timeContext;
    private TextView timeSplashText;

    private String timeDialogMessage;
    private int timeDefault,timeValue = 5;

    private NumberPicker hourPicker,minutePicker;

    public TimeSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        timeContext = context;

        timeDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");

        timeDefault = (int) attrs.getAttributeIntValue(androidns,"defaultValue",5);
    }

    //Create a dialog box
    @Override
    protected View onCreateDialogView() {

        TableLayout layout = new TableLayout(timeContext);
        layout.setPadding(6,6,6,6);
        timeSplashText = new TextView(timeContext);

        if (timeDialogMessage != null){
            timeSplashText.setText(timeDialogMessage);
        }


        TableRow row_header = new TableRow(timeContext);
        row_header.addView(timeSplashText);

        hourPicker = new NumberPicker(timeContext);
        minutePicker = new NumberPicker(timeContext);


        //Disable soft keyboard
        hourPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minutePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        minutePicker.setMaxValue(MINUTE_MAX_VALUE);
        minutePicker.setMinValue(MINUTE_MIN_VALUE);
        hourPicker.setMaxValue(HOUR_MAX_VALUE);
        hourPicker.setMinValue(HOUR_MIN_VALUE);

        TextView hour = new TextView(timeContext);
        if (hourPicker.getValue() != 1)
        hour.setText("hours");
        else
        hour.setText("hour");
        hour.setTextSize(18);

        TextView minute = new TextView(timeContext);
        if (hourPicker.getValue() != 1)
            minute.setText("minutes");
        else
            minute.setText("minute");
        minute.setTextSize(18);

        TableRow row_one = new TableRow(timeContext);
        row_one.setGravity(Gravity.CENTER);
        row_one.addView(hourPicker);
        row_one.addView(hour);
        row_one.addView(minutePicker);
        row_one.addView(minute);

        layout.addView(row_header);

        TableLayout table_main = new TableLayout(timeContext);
        table_main.addView(row_one);

        TableRow row_main = new TableRow(timeContext);
        row_main.setGravity(Gravity.CENTER_HORIZONTAL);
        row_main.addView(table_main);

        layout.addView(row_main);

        try {
            if (shouldPersist())
                timeValue = getPersistedInt(timeDefault);
        }

        catch (Exception ex){
            timeValue = TIME_DEFAULT;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(timeContext);
        temperatureSample = sharedPreferences.getInt("TemperatureSampleTime", 1);
        bindData();

        return layout;
    }

    //Set up display values and default if necessary
    private void bindData(){
        int time,minutes,seconds;
        time = timeValue;
        minutes = time%3600;
        seconds = minutes%60;
        time = time - seconds;
        if(time > 0){
            minutes = (timeValue%3600);
            minutePicker.setValue((int) minutes/60);
            time = time - minutes;
            if(time > 0){
                hourPicker.setValue((int) (time/3600));
            }
        }

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        bindData();
    }


    //Check that values are not outside bounds and save
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            super.onDialogClosed(true);
            timeValue = (hourPicker.getValue()*60*60) + (minutePicker.getValue()*60);


            if(shouldPersist())
                persistInt(timeValue);

        }
        callChangeListener(timeValue);
    }

    //Set values from saved values

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue,defaultValue);
        if(restorePersistedValue){
            try{
                timeValue = shouldPersist() ? getPersistedInt(timeDefault) : 0;
            }
            catch (Exception ex){
                timeValue =  timeDefault;
            }

        }
        else timeValue = (int) defaultValue;
    }

}


