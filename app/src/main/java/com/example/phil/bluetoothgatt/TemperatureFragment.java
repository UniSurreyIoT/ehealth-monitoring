/**
 * Created by Phil on 16/01/2016.
 * Fragment intended to hold graphical representation of Temperature data
 * Allows for realtime and stored data to be displayed
 */
package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TemperatureFragment extends Fragment implements View.OnClickListener, OnChartValueSelectedListener, RadioGroup.OnCheckedChangeListener, RadioButton.OnCheckedChangeListener {

    private boolean debug = false;

    protected Activity mActivity;

    private LineChart mGraphView;
    private LineData linedata;
    private Activity activity;
    private RadioButton temperatureFitAllButton,temperatureSelectRangeButton;
    private CheckBox temperatureRealtimeCheckbox,temperatureAllowScrollCheckbox;
    private DatabaseHelper db;
    private TimeDialog timeDialog;
    private VisableSampleSelector sampleDialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int visableSampleSize;

    public String TEMPERATURE_VISABLE_SAMPLE = "temperature_visable_sample_size";

    //Fragment attached to Activity
    //Checks if Activity listening for callbacks
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        activity = getActivity();
        try {
            this.mConnectionState = (onConnectionStateChangeListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

    //Creates fragement, reads preferences and tells fragment to retains choices
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timeDialog = new TimeDialog(this.getActivity());
        sampleDialog = new VisableSampleSelector(this.getActivity());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());

        // retain this fragment
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    //Creates the user interface
    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle SavedInstanceState){

        View view = inflator.inflate(R.layout.fragment_temperature,container,false);
        mGraphView = (LineChart) view.findViewById(R.id.temperatureGraph);


        temperatureFitAllButton = (RadioButton) view.findViewById(R.id.temperatureFitAll);
        temperatureSelectRangeButton = (RadioButton) view.findViewById(R.id.temperatureSelectRange);
        RadioButton temperatureReadSaved = (RadioButton) view.findViewById(R.id.temperatureReadSaved);

        temperatureAllowScrollCheckbox = (CheckBox) view.findViewById(R.id.temperatureAllowScroll);
        temperatureAllowScrollCheckbox.setOnCheckedChangeListener(this);
        temperatureAllowScrollCheckbox.setEnabled(false);
        temperatureRealtimeCheckbox = (CheckBox) view.findViewById(R.id.temperatureRealtime);
        temperatureRealtimeCheckbox.setEnabled(false);

        RadioGroup temperatureGraphOptions = (RadioGroup) view.findViewById(R.id.temperatureGraphOptions);
        temperatureGraphOptions.setOnCheckedChangeListener(this);

        mGraphView.setOnChartValueSelectedListener(this);
        mGraphView.setAutoScaleMinMaxEnabled(true);

        YAxis rightAxis = mGraphView.getAxisRight();
        rightAxis.setEnabled(false);

        YAxis leftAxis = mGraphView.getAxisLeft();
        leftAxis.setStartAtZero(false);
        leftAxis.mDecimals=2;
        leftAxis.setValueFormatter(new YValueFormatter());

        mGraphView.setHighlightPerTapEnabled(true);

        linedata =  new LineData();
        linedata.setValueFormatter(new GraphValueFormatter());
        mGraphView.setDescription("Temperature");
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        BluetoothManager manager = (BluetoothManager) activity.getSystemService(activity.BLUETOOTH_SERVICE);
        List<BluetoothDevice> devices = manager.getConnectedDevices(BluetoothProfile.GATT);
        for(BluetoothDevice device : devices) {
            if(device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                if(device.getAddress().equals(sharedPreferences.getString("SelectedModuleAddress", null)))
                    Log.i("Device","connected");
            }
        }
        activity.registerReceiver(mDisplayGraphReceiver, makeDisplayGraphIntentFilter());
        mGraphView.setData(linedata);
        Context context = this.getActivity().getApplicationContext();
        db = new DatabaseHelper(context);
        visableSampleSize = sharedPreferences.getInt(TEMPERATURE_VISABLE_SAMPLE,20);
    }

    @Override
    public void onPause(){
        super.onPause();
        activity.unregisterReceiver(mDisplayGraphReceiver);
    }

    //Creates the custom menu for this fragment
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.temperature_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Menu items selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean connectionState = mConnectionState.connectionState();

        switch(item.getItemId()){
            case R.id.setTemperatureSampleTime:

                if(connectionState) {
                    readSampleCharacteristic();
                }
                else {
                    Toast.makeText(getActivity(), R.string.device_not_connected, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.TemperatureSettings:
                getSampleSettings();
                break;

            case R.id.TemperatureDataCompression:
                if(connectionState) {
                    Intent intent = new Intent(this.getActivity(), TemperaturePreferences.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getActivity(), R.string.device_not_connected, Toast.LENGTH_SHORT).show();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    //Returns sample settings to Activity
    private void getSampleSettings(){
        sampleDialog.setCustomEventListener(new VisableSampleSelector.DialogListener() {
            @Override
            public void userSelectedAValue(int samples) {
                Log.i("Number of samples",String.valueOf(samples));
                editor = sharedPreferences.edit();
                editor.putInt(TEMPERATURE_VISABLE_SAMPLE,samples);
                visableSampleSize = samples;
                editor.apply();
            }


        });

        sampleDialog.setTitle("Visable Samples");
        sampleDialog.show();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                int sampleTime = data.getIntExtra("timeValue", 0);
                Log.i("SAmpletime",String.valueOf(sampleTime));
            }
        }
    }

    //Send settings to BLE device via BluetoothLeService
    private void broadcastUpdate(final int value) {
        final Intent intent = new Intent(BluetoothLeService.CHARACTERISTIC_WRITE);
        intent.putExtra("parentService", GattAttributes.TEMPERATURESERVICE);

        intent.putExtra("characteristic", GattAttributes.TEMPERATURESAMPLECHAR);

        intent.putExtra("value",value);

        intent.putExtra("type", 4);

        Log.i("Broadcast", "sent");
        getActivity().sendBroadcast(intent);
    }

    private void readSampleCharacteristic(){
        final Intent intent = new Intent ( BluetoothLeService.CHARACTERISTIC_READ);
        intent.putExtra("parentService", GattAttributes.TEMPERATURESERVICE);
        intent.putExtra("characteristic", GattAttributes.TEMPERATURESAMPLECHAR);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            default:

        }

    }


    public void onValueSelected(Entry e, int dataSetIndex, Highlight h){
   }

    public void onNothingSelected(){

    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean checked){
        switch(button.getId()){
            case R.id.temperatureAllowScroll:
                if(temperatureAllowScrollCheckbox.isChecked()){
                    mGraphView.setDragEnabled(true);
                }
                else{
                    mGraphView.setDragEnabled(false);
                }
                break;
            case R.id.temperatureRealtime:
                if(temperatureFitAllButton.isChecked()){
                    mGraphView.fitScreen();
                    mGraphView.moveViewToX(0);
                }
                else{
                    linedata = mGraphView.getData();
                    if (linedata != null) {
                        LineDataSet set = linedata.getDataSetByIndex(0);
                        if(set!=null) {
                            int xmin, xmax;
                            xmax = set.getEntryCount();
                            xmin = xmax - visableSampleSize;
                            if (xmin < 0) {
                                xmin = 0;
                            }
                            mGraphView.moveViewToX(xmin);
                        }
                    }
                }
                break;

        }

    }
    boolean readSaved = false;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // checkedId is the RadioButton selected
        switch(checkedId){
            case R.id.temperatureFitAll:
                    temperatureAllowScrollCheckbox.setEnabled(false);
                    temperatureAllowScrollCheckbox.setChecked(false);
                    if(readSaved){
                        live = true;
                        mGraphView.clear();
                        linedata = new LineData();
                        linedata.setValueFormatter(new GraphValueFormatter());
                        mGraphView.setData(linedata);
                        readSaved = false;
                    }
                    mGraphView.fitScreen();
                    temperatureRealtimeCheckbox.setChecked(true);
                    temperatureRealtimeCheckbox.setEnabled(false);
                    mGraphView.moveViewToX(0);


                break;

            case R.id.temperatureSelectRange:
                temperatureAllowScrollCheckbox.setEnabled(true);
                temperatureRealtimeCheckbox.setEnabled(true);
                if(temperatureAllowScrollCheckbox.isChecked()) {
                    mGraphView.setDragEnabled(true);
                    mGraphView.setPinchZoom(true);
                }
                else {
                    mGraphView.setDragEnabled(false);
                    mGraphView.setPinchZoom(false);
                }
                if(temperatureRealtimeCheckbox.isChecked()) {
                    linedata = mGraphView.getData();
                    if (linedata != null) {
                        LineDataSet set = linedata.getDataSetByIndex(0);
                        if(set!=null) {
                            int xmin, xmax;
                            xmax = set.getEntryCount();
                            xmin = xmax - visableSampleSize;
                            if (xmin < 0) {
                                xmin = 0;
                            }
                            mGraphView.moveViewToX(xmin);
                        }
                    }
                }
                else{
                    mGraphView.setVisibleXRangeMaximum(visableSampleSize);
                }
                Log.i("Checkbox","AllowScroll");
                mGraphView.invalidate();
                break;

            case R.id.temperatureReadSaved:
                temperatureAllowScrollCheckbox.setEnabled(false);
                temperatureAllowScrollCheckbox.setChecked(true);
                mGraphView.setDragEnabled(true);
                mGraphView.setPinchZoom(true);
                readSaved = true;
                readSavedValues();
                Log.i("Checkbox","Realtime");
                break;
        }

    }


    ProgressDialog pd;
    private void readSavedValues(){


        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(getContext());
                pd.setTitle("Collecting Data...");
                pd.setMessage("Please wait.");
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    //Do something...
                    live = false;
                    List <ReadingClass> readings;
                    readings = db.getReadings(BluetoothLeService.TEMPERATURE);
                    linedata.clearValues();// = mGraphView.getData();
                    if (linedata != null) {
                        LineDataSet set = linedata.getDataSetByIndex(0);

                        if (set == null) {
                            set = createSet();
                            set.setValueFormatter(new GraphValueFormatter());
                            linedata.addDataSet(set);

                        }
                        for (ReadingClass reading : readings) {
                            SimpleDateFormat sdf = new SimpleDateFormat("HHmmss_ddMMyyyy",Locale.ENGLISH);
                            String currentDateandTime = sdf.format(reading.getTime()*1000);
                            linedata.addXValue(currentDateandTime);
                            linedata.addEntry(
                                    new Entry((float) reading.getValueInt()/100, set.getEntryCount()), 0);
    }
                        mGraphView.notifyDataSetChanged();
                        mGraphView.fitScreen();
                        mGraphView.moveViewToX(0);
                    }
                } catch (Exception e) {
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
            }

        };
        task.execute((Void[])null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private static IntentFilter makeDisplayGraphIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_TEMPERATURE_UPDATE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    private boolean live = true;
    private final BroadcastReceiver mDisplayGraphReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(BluetoothLeService.ACTION_TEMPERATURE_UPDATE.equals(action) && live){
                try{
                    float value = ((float) intent.getIntExtra("value",-1))/100;
                    updateGraph(value);
                }
                catch(NumberFormatException ex) {
                }
            }

            else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){
                if(intent.getStringExtra("characteristic").equals(GattAttributes.TEMPERATURESAMPLECHAR)){

                    editor = sharedPreferences.edit();
                    int timeValue = intent.getIntExtra("value", 1);
                    editor.putInt("TemperatureSampleTime",(int) timeValue);
                    editor.apply();
                    timeDialog.setCustomEventListener(new TimeDialog.DialogListener() {
                        @Override
                        public void userSelectedAValue(int hours, int minutes, int seconds) {
                            Log.i("Hours",String.valueOf(hours));
                            int value = (hours*60*60) + (minutes*60) + seconds;
                            editor.putInt("TemperatureSample",value);
                            broadcastUpdate(value);
                        }

                        @Override
                        public void userCanceled() {

                        }
                    });
                    int remainder,minutes,seconds;
                    minutes = timeValue%3600;
                    seconds = minutes%60;
                    timeDialog.secondsPicker.setValue(seconds);
                    timeValue = timeValue - seconds;
                    if(timeValue > 0){
                        minutes = (timeValue%3600);
                        timeDialog.minutePicker.setValue(minutes/60);
                        timeValue = timeValue - minutes;
                        if(timeValue > 0){
                            timeDialog.hourPicker.setValue(timeValue/3600);
                        }
                    }
                    timeDialog.setTitle("Temperature Sample Interval");
                    timeDialog.show();


                }

            }
        }
    };

    private LineDataSet createSet(){
        return new LineDataSet(null,"Temperature");

    }


    private void updateGraph(float value){
        if(value != -1/100) {


            linedata = mGraphView.getData();
            if (linedata != null) {
                LineDataSet set = linedata.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet();
                    set.setValueFormatter(new GraphValueFormatter());
                    linedata.addDataSet(set);
                }
                SimpleDateFormat sdf = new SimpleDateFormat("HHmmss_ddMMyyyy", Locale.ENGLISH);
                String currentDateandTime = sdf.format(new Date());
                linedata.addXValue(currentDateandTime);
                linedata.addEntry(
                        new Entry(value, set.getEntryCount()), 0);
                mGraphView.notifyDataSetChanged();
                if (temperatureRealtimeCheckbox.isChecked()) {
                    mGraphView.fitScreen();

                    int xmin, xmax;
                    xmax = set.getEntryCount();
                    xmin = xmax - visableSampleSize;
                    if (xmin < 0) {
                        xmin = 0;
                    }
                    mGraphView.moveViewToX(xmin);
                }

                if (temperatureFitAllButton.isChecked()) {
                    mGraphView.moveViewToX(0);
                }
                else if (temperatureSelectRangeButton.isChecked()) {
                    mGraphView.setVisibleXRangeMaximum(visableSampleSize);
                }
            }
        }
    }

    onConnectionStateChangeListener mConnectionState;

    public interface onConnectionStateChangeListener{
        public boolean connectionState();
    }



}