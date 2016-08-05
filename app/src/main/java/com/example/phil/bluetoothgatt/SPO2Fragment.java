/**
 * Created by Phil on 16/01/2016.
 * Fragment intended to hold graphical representation of SPO2 data
 */
package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.List;

public class SPO2Fragment extends Fragment implements View.OnClickListener, OnChartValueSelectedListener {

    private boolean debug = false;

    protected Activity mActivity;
    Context applicationContext;

    private LineChart mGraphView;
    private LineData linedata;
    private Activity activity;
    private SharedPreferences sharedPreferences;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        applicationContext = context.getApplicationContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle SavedInstanceState) {

        View view = inflator.inflate(R.layout.fragment_spo2, container, false);
        mGraphView = (LineChart) view.findViewById(R.id.spo2Graph);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        activity.unregisterReceiver(mDisplayGraphReceiver);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                return;
        }

    }


    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
    }

    public void onNothingSelected() {

    }




    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private static IntentFilter makeDisplayGraphIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_SPO2_UPDATE);
        return intentFilter;
    }

    private boolean live = true;
    private final BroadcastReceiver mDisplayGraphReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_SPO2_UPDATE.equals(action) && live) {
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
                            mGraphView.moveViewToX(0);
                        }

                    }
                } catch (NumberFormatException ex) {

                }
            }
        }
    };

    private LineDataSet createSet() {
        return new LineDataSet(null, "SpO2");

    }


}