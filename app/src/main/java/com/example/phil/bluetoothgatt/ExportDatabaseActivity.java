/**
 * Created by Phil on 16/01/2016.
 * Activity for selecting details for exporting data
 */

package com.example.phil.bluetoothgatt;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class ExportDatabaseActivity extends FragmentActivity implements View.OnClickListener,SelectExportDialog.OnCompleteListener,SelectTimeDialog.OnCompleteListener,SelectDateDialog.OnCompleteListener
{
    boolean []  a ={false,false,false,false,false,false,false,false,false};
    private View selectExportButton;
    private View selectStartTime;
    private View selectEndTime;
    private View selectStartDate;
    private View selectEndDate;
    private View selectExport;
    private View selectExportEmail;

    private TextView startTimeText;
    private TextView startDateText;
    private TextView endTimeText;
    private TextView endDateText;

    private int startTimeHour;
    private int startTimeMinute;
    private int endTimeHour;
    private int endTimeMinute;
    private int startTimeDay = 1;
    private int startTimeMonth = 0;
    private int startTimeYear = 2016;
    private int endTimeDay;
    private int endTimeMonth;
    private int endTimeYear;

    private long startTime;
    private long endTime;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exportdatabase);

        selectExport = findViewById(R.id.selectTypes);
        selectExport.setOnClickListener(this);

        selectStartTime = findViewById(R.id.selectStartTime);
        selectStartTime.setOnClickListener(this);
        selectStartTime.setEnabled(false);

        selectEndTime = findViewById(R.id.selectEndTime);
        selectEndTime.setOnClickListener(this);
        selectEndTime.setEnabled(false);

        selectStartDate = findViewById(R.id.selectStartDate);
        selectStartDate.setOnClickListener(this);
        selectStartDate.setEnabled(false);

        selectEndDate = findViewById(R.id.selectEndDate);
        selectEndDate.setOnClickListener(this);
        selectEndDate.setEnabled(false);

        selectExportButton = findViewById(R.id.runExport);
        selectExportButton.setOnClickListener(this);
        selectExportButton.setEnabled(false);

        selectExportEmail = findViewById(R.id.runExportEmail);
        selectExportEmail.setOnClickListener(this);
        selectExportEmail.setEnabled(false);

        startTimeText = (TextView)findViewById(R.id.startTimeText);
        startDateText = (TextView)findViewById(R.id.startDateText);

        endTimeText = (TextView)findViewById(R.id.endTimeText);
        endDateText = (TextView)findViewById(R.id.endDateText);
    }

    @Override
    public void onClick(View v) {
        Log.i("View", String.valueOf(v.getId()));
        switch(v.getId()){
            case R.id.selectTypes:
                DialogFragment newFragment = SelectExportDialog.newInstance(a);
                newFragment.show(getFragmentManager(), "exportDialog");
                break;
            case R.id.selectEndTime:
                DialogFragment newFragment1 = SelectTimeDialog.newInstance(R.id.selectEndTime,endTimeHour,endTimeMinute,"Select End Time");
                newFragment1.show(getFragmentManager(), "endDialog");
                break;
            case R.id.selectStartTime:
                DialogFragment newFragment2 = SelectTimeDialog.newInstance(R.id.selectStartTime,startTimeHour,startTimeMinute,"Select Start Time");
                newFragment2.show(getFragmentManager(), "startDialog");
                break;
            case R.id.selectStartDate:
                DialogFragment newFragment3 = SelectDateDialog.newInstance(R.id.selectStartDate,startTimeYear,startTimeMonth,startTimeDay,"Select Start Date");
                newFragment3.show(getFragmentManager(),"startDateDialog");
                break;
            case R.id.selectEndDate:
                DialogFragment newFragment4 = SelectDateDialog.newInstance(R.id.selectEndDate,endTimeYear,endTimeMonth,endTimeDay,"Select End Date");
                newFragment4.show(getFragmentManager(),"endDateDialog");
                break;
            case R.id.runExport:
                export(this,mExportList,startTime,endTime);
                break;
            case R.id.runExportEmail:
                export(this,mExportList,startTime,endTime);
                break;
        }
    }

    ProgressDialog pd;
    DatabaseHelper db = new DatabaseHelper(this);

    private void export(final Context context,final List<String> exportList,final long startTime, final long endTime){


        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(context);
                pd.setTitle("Collecting Data to Export...");
                pd.setMessage("Please wait.");
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    db.getReadings(exportList,startTime/1000,endTime/1000,getApplicationContext());
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (pd!=null) {
                    pd.dismiss();

                }
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String filename = sharedPreferences.getString("currentFile",null);
                if(filename != null) {
                    Intent intent = new Intent(getBaseContext(), MailSenderActivity.class);
                    Bundle emailBundle = new Bundle();

                    emailBundle.putString("EmailFile", filename);
                    intent.putExtras(emailBundle);
                    startActivity(intent);


                }
                finish();
            }

        };
        task.execute((Void[])null);
    }


    List <String> mExportList = new ArrayList<>();

    boolean exportTypeSelected;
    @Override
    public void onComplete(boolean[] array) {
        if(array[0]) {
            mExportList.add(BluetoothLeService.PULSE_OXYGEN_SATURATION);
            exportTypeSelected = true;
        }
        if(array[1]) {
            mExportList.add(BluetoothLeService.PULSE_BPM);
            exportTypeSelected = true;
        }
        if(array[2]) {
            mExportList.add(BluetoothLeService.ECG);
            exportTypeSelected = true;
        }
        if(array[3]) {
            mExportList.add(BluetoothLeService.EMG);
            exportTypeSelected = true;
        }
        if(array[4]) {
            mExportList.add(BluetoothLeService.AIRFLOW);
            exportTypeSelected = true;
        }
        if(array[5]) {
            mExportList.add(BluetoothLeService.TEMPERATURE);
            exportTypeSelected = true;
        }
        if(array[6]) {
            mExportList.add(BluetoothLeService.BLOODPRESSURE);
            exportTypeSelected = true;
        }
        if(array[7]) {
            mExportList.add(BluetoothLeService.POSITION);
            exportTypeSelected = true;
        }
        if(array[8]) {
                mExportList.add(BluetoothLeService.GLUCOMETER);
                exportTypeSelected = true;
        }
        a = array;
        if(exportTypeSelected){
            selectStartTime.setEnabled(true);
        }
    }

    @Override
    public void onComplete(int hour,int minute, int id) {
        String hourString,minuteString;
        if(hour < 10)
            hourString = "0";
        else
            hourString = "";
        if(minute < 10)
            minuteString = "0";
        else
            minuteString = "";
        switch(id){
            case R.id.selectStartTime:
                startTimeHour = endTimeHour = hour;
                startTimeMinute = endTimeMinute = minute;

                hourString += String.valueOf(startTimeHour);
                minuteString += String.valueOf(startTimeMinute);
                startTimeText.setText( hourString + ":" + minuteString);
                selectStartDate.setEnabled(true);
                break;
            case R.id.selectEndTime:
                endTimeHour = hour;
                endTimeMinute = minute;

                hourString += String.valueOf(endTimeHour);
                minuteString += String.valueOf(endTimeMinute);
                endTimeText.setText( hourString + ":" + minuteString);

                selectEndDate.setEnabled(true);
                break;
        }
    }

    Calendar start = Calendar.getInstance();
    Calendar end = Calendar.getInstance();

    @Override
    public void onComplete(int day, int month, int year, int id) {
        switch(id){
            case R.id.selectStartDate:
                startTimeDay = endTimeDay = day;
                startTimeMonth = endTimeMonth = month + 1;
                startTimeYear = endTimeYear = year;
                startDateText.setText(String.valueOf(startTimeDay) + "/" + String.valueOf(startTimeMonth) + "/" + String.valueOf(startTimeYear));

                start.set(startTimeYear,startTimeMonth - 1 ,startTimeDay,startTimeHour,startTimeMinute);
                startTime = start.getTimeInMillis();
                selectEndTime.setEnabled(true);
                break;

            case R.id.selectEndDate:
                endTimeDay = day;
                endTimeMonth = month + 1;
                endTimeYear = year;
                //Calendar end = null;
                end.set(endTimeYear,endTimeMonth - 1,endTimeDay,endTimeHour,endTimeMinute);
                endTime = end.getTimeInMillis();
                if(endTime < startTime){
                    endTimeMinute = 0;
                    endTimeDay = 0;
                    endTimeYear = 0;
                    endTimeMonth = 0;
                    endTimeHour = 0;
                    endTimeText.setText(String.valueOf(endTimeHour) + ":" + String.valueOf(endTimeMinute));
                    selectEndDate.setEnabled(false);
                }

                else{
                    selectExportButton.setEnabled(true);
                    selectExportEmail.setEnabled(true);
                }
                endDateText.setText(String.valueOf(endTimeDay) + "/" + String.valueOf(endTimeMonth) + "/" + String.valueOf(endTimeYear));

                break;

        }
    }
}