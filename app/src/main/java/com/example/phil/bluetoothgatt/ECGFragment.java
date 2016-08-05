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
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Phil on 15/01/2016.
 */
public class ECGFragment extends Fragment implements View.OnClickListener, OnChartValueSelectedListener, RadioGroup.OnCheckedChangeListener, RadioButton.OnCheckedChangeListener {

    private boolean debug = false;

    private View sampleButton;
    private View stopButton;
    protected Activity mActivity;
    private TextView textView;
    private CheckBox mCheckbox;
    private boolean checkboxSet;
    Context applicationContext;

    private LineChart mGraphView;
    private LineData linedata;
    private Activity activity;
    private NumberPicker mNumberPicker;
    private EditText mEditText;
    private RadioGroup ecgGraphOptions;
    private RadioButton ecgUncompressedButton, ecgSAXButton, temperatureReadSaved;
    private CheckBox temperatureRealtimeCheckbox, temperatureAllowScrollCheckbox;
    private DatabaseHelper db;
    private Button loadButton;
    private TimeDialog timeDialog;
    private VisableSampleSelector sampleDialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int visableSampleSize;
    public String TEMPERATURE_VISABLE_SAMPLE = "temperature_visable_sample_size";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        applicationContext = context.getApplicationContext();
        activity = getActivity();

    }


    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle SavedInstanceState) {

        View view = inflator.inflate(R.layout.fragment_ecg, container, false);

        mGraphView = (LineChart) view.findViewById(R.id.ecgGraph);

        sampleButton = (Button) view.findViewById(R.id.ecgSampleButton);
        sampleButton.setOnClickListener(this);

        stopButton = (Button) view.findViewById(R.id.ecgStopButton);
        stopButton.setOnClickListener(this);
        stopButton.setEnabled(false);

        ecgUncompressedButton = (RadioButton) view.findViewById(R.id.ecgUncompressedButton);
        ecgSAXButton = (RadioButton) view.findViewById(R.id.ecgSAXButton);
        //temperatureReadSaved = (RadioButton) view.findViewById(R.id.temperatureReadSaved);

        //temperatureAllowScrollCheckbox = (CheckBox) view.findViewById(R.id.temperatureAllowScroll);
        //temperatureRealtimeCheckbox = (CheckBox) view.findViewById(R.id.temperatureRealtime);
        //temperatureRealtimeCheckbox.setEnabled(false);

        ecgGraphOptions = (RadioGroup) view.findViewById(R.id.ecgOutputType);
        ecgGraphOptions.setOnCheckedChangeListener(this);

        mGraphView.setOnChartValueSelectedListener(this);
        mGraphView.setAutoScaleMinMaxEnabled(true);

        YAxis rightAxis = mGraphView.getAxisRight();
        rightAxis.setEnabled(false);

        YAxis leftAxis = mGraphView.getAxisLeft();
        leftAxis.setStartAtZero(false);
        leftAxis.mDecimals = 2;
        leftAxis.setValueFormatter(new YValueFormatter());

        mGraphView.setHighlightPerTapEnabled(true);

        linedata = new LineData();
        linedata.setValueFormatter(new GraphValueFormatter());

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());

        // retain this fragment
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }
        return super.onOptionsItemSelected(item);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                int sampleTime = data.getIntExtra("timeValue", 0);
                Log.i("SAmpletime", String.valueOf(sampleTime));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BluetoothManager manager = (BluetoothManager) activity.getSystemService(activity.BLUETOOTH_SERVICE);
        List<BluetoothDevice> devices = manager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice device : devices) {
            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                if (device.getAddress().equals(sharedPreferences.getString("SelectedModuleAddress", null)))
                    Log.i("Device", "connected");
            }
        }
        activity.registerReceiver(mDisplayGraphReceiver, makeDisplayGraphIntentFilter());
        mGraphView.setData(linedata);
        Context context = this.getActivity().getApplicationContext();
        db = new DatabaseHelper(context);
        visableSampleSize = sharedPreferences.getInt("ECG_VISABLE_SAMPLE", 20);
        //mGraphView.setVisibleXRangeMaximum(40);
    }

    @Override
    public void onPause() {
        super.onPause();
        activity.unregisterReceiver(mDisplayGraphReceiver);
    }

    private void broadcastUpdate(final int value) {
        final Intent intent = new Intent(BluetoothLeService.CHARACTERISTIC_WRITE);
        intent.putExtra("parentService", GattAttributes.TEMPERATURESERVICE);

        intent.putExtra("characteristic", GattAttributes.TEMPERATURESAMPLECHAR);

        intent.putExtra("value", value);

        intent.putExtra("type", 4);

        Log.i("Broadcast", "sent");
        getActivity().sendBroadcast(intent);
    }





    private void readSampleCharacteristic() {
        final Intent intent = new Intent(BluetoothLeService.CHARACTERISTIC_READ);
        intent.putExtra("parentService", GattAttributes.TEMPERATURESERVICE);
        intent.putExtra("characteristic", GattAttributes.TEMPERATURESAMPLECHAR);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ecgSampleButton:

                stopButton.setEnabled(true);
                sampleButton.setEnabled(false);
                broadcastUpdate(GattAttributes.ECGSERVICE, GattAttributes.USEECGCHAR, 1, 1);
                if(sharedPreferences.getBoolean("hasPulsioximeter",false)) {
                    broadcastUpdate(GattAttributes.PULSIOXIMETERSERVICE, GattAttributes.USEBPMCHAR, 0, 1);
                    broadcastUpdate(GattAttributes.PULSIOXIMETERSERVICE, GattAttributes.USESPO2CHAR, 0, 1);

                }

                if(sharedPreferences.getBoolean("hasTemperature",false)) {
                    broadcastUpdate(GattAttributes.TEMPERATURESERVICE, GattAttributes.USETEMPERATURECHAR, 0, 1);
                }

                return;

            case R.id.ecgStopButton:
                stopButton.setEnabled(false);
                sampleButton.setEnabled(true);
                broadcastUpdate(GattAttributes.ECGSERVICE, GattAttributes.USEECGCHAR,0,1);

                if(sharedPreferences.getBoolean("hasPulsioximeter",false)) {
                    if (sharedPreferences.getBoolean("collectBPMCheckbox", false)) {
                        broadcastUpdate(GattAttributes.PULSIOXIMETERSERVICE, GattAttributes.USEBPMCHAR, 1, 1);
                    }
                    if (sharedPreferences.getBoolean("collectSPO2Checkbox", false)) {
                        broadcastUpdate(GattAttributes.PULSIOXIMETERSERVICE, GattAttributes.USESPO2CHAR, 1, 1);

                    }
                }

                if(sharedPreferences.getBoolean("hasTemperature",false)) {
                    if(sharedPreferences.getBoolean("collectTemperatureCheckbox",false)){
                        broadcastUpdate(GattAttributes.TEMPERATURESERVICE, GattAttributes.USETEMPERATURECHAR, 1, 1);
                    }
                }
                return;

            default:
                return;
        }

    }


    private void broadcastUpdate(String parentService, String characteristicString, final int value,int type) {
        final Intent intent = new Intent(BluetoothLeService.CHARACTERISTIC_WRITE);
        intent.putExtra("parentService", parentService);

        intent.putExtra("characteristic", characteristicString);

        intent.putExtra("value",value);

        intent.putExtra("type",type);

        Log.i("Broadcast", "sent");
        getActivity().sendBroadcast(intent);
    }

    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
    }

    public void onNothingSelected() {

    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        switch (button.getId()) {

        }

    }

    boolean readSaved = false;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // checkedId is the RadioButton selected
        mGraphView.clear();
        linedata = new LineData();
        linedata.setValueFormatter(new GraphValueFormatter());
        mGraphView.setData(linedata);

        mGraphView.fitScreen();
        mGraphView.moveViewToX(0);


        switch (checkedId) {
            case R.id.ecgUncompressedButton:
                broadcastUpdate(GattAttributes.ECGSERVICE, GattAttributes.ECGOUTPUTTYPECHAR,0,1);

                break;
            case R.id.ecgSAXButton:
                broadcastUpdate(GattAttributes.ECGSERVICE, GattAttributes.ECGOUTPUTTYPECHAR,1,1);

                break;
            }

    }

    ProgressDialog pd;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private static IntentFilter makeDisplayGraphIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_ECG_UPDATE);
        intentFilter.addAction(BluetoothLeService.ACTION_ECG_ALPHA_UPDATE);
        return intentFilter;
    }

    private boolean live = true;
    private final BroadcastReceiver mDisplayGraphReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_ECG_UPDATE.equals(action) && live) {
                try {
                    float value = ((float) intent.getIntExtra("value", -1)) / 100;
                    if (value != -1 / 100) {
                        linedata = mGraphView.getData();
                        if (linedata != null) {
                            LineDataSet set = linedata.getDataSetByIndex(0);

                            if (set == null) {
                                Log.i("set", "notnull");
                                set = createSet();
                                set.setValueFormatter(new GraphValueFormatter());
                                linedata.addDataSet(set);
                            }
                            linedata.addXValue(set.getEntryCount() + "");
                            linedata.addEntry(
                                    new Entry(value, set.getEntryCount()), 0);
                            mGraphView.notifyDataSetChanged();
                            int xmin, xmax;
                            xmax = set.getEntryCount();
                            xmin = xmax - 100;
                            if (xmin < 0) {
                                xmin = 0;
                            }
                            mGraphView.moveViewToX(xmin);
                            mGraphView.setVisibleXRangeMaximum(100);
                        }
                    }
                } catch (NumberFormatException ex) {

                }

            }

            else if (BluetoothLeService.ACTION_ECG_ALPHA_UPDATE.equals(action)) {
                String alphaString = intent.getStringExtra("value");
                char[] alphaChar = alphaString.toCharArray();
                float alphaFloat[] = new float[alphaChar.length];
                for(int i = 0; i < alphaChar.length; i++){
                    switch(alphaChar[i]) {
                        case 'a':
                            alphaFloat[i] = (float) -1.15;
                            break;
                        case 'b':
                            alphaFloat[i] = (float) -0.67;
                            break;
                        case 'c':
                            alphaFloat[i] = (float) -0.32;
                            break;
                        case 'd':
                            alphaFloat[i] = (float) 0;
                            break;
                        case 'e':
                            alphaFloat[i] = (float) 0.32;
                            break;
                        case 'f':
                            alphaFloat[i] = (float) 0.67;
                            break;
                        case 'g':
                            alphaFloat[i] = (float) 1.15;
                            break;
                        case 'h':
                            alphaFloat[i] = (float) 2;
                    }
                }

                for(int i = 0; i < alphaFloat.length; i++){

                    linedata = mGraphView.getData();
                    if (linedata != null) {
                        LineDataSet set = linedata.getDataSetByIndex(0);

                        if (set == null) {
                            set = createSet();
                            set.setValueFormatter(new GraphValueFormatter());
                            linedata.addDataSet(set);
                        }
                        linedata.addXValue(set.getEntryCount() + "");
                        linedata.addEntry(
                                new Entry(alphaFloat[i], set.getEntryCount()), 0);
                        mGraphView.notifyDataSetChanged();

                        int xmin, xmax;
                        xmax = set.getEntryCount();
                        xmin = xmax - 100;
                        if (xmin < 0) {
                            xmin = 0;
                        }
                        mGraphView.moveViewToX(xmin);
                        mGraphView.setVisibleXRangeMaximum(100);
                    }


                }

            }
        }
    };

    private LineDataSet createSet() {
        return new LineDataSet(null, "ECG");

    }


}