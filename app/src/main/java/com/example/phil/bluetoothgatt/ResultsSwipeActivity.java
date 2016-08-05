package com.example.phil.bluetoothgatt;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;


/**
 * Created by Phil on 14/01/2016.
 */
public class ResultsSwipeActivity extends AppCompatActivity implements TemperatureFragment.onConnectionStateChangeListener{

    private ViewPager viewPager;
    //private ActionBar actionBar;
    //private PagerAdapterClass pagerAdapterClass;
    TabLayout tablayout;
    private int temp;

    private int moduleOptions;
    public Context context;
    public int getTemp(){return temp;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature((Window.FEATURE_ACTION_BAR));
        setContentView(R.layout.activity_resultsswipe);

        Toolbar toolbar = (Toolbar) findViewById((R.id.toolbar));
        setSupportActionBar(toolbar);
        context = this.getApplicationContext();
        tablayout = (TabLayout) findViewById(R.id.tab_layout);
        int x = 1;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(sharedPreferences.getBoolean("collectSPO2Checkbox",false))
                tablayout.addTab(tablayout.newTab().setText(R.string.SPO2));
        if(sharedPreferences.getBoolean("collectBPMCheckbox",false))
            tablayout.addTab(tablayout.newTab().setText(R.string.BPM));
        if(sharedPreferences.getBoolean("collectECGCheckbox",false))
            tablayout.addTab(tablayout.newTab().setText(R.string.ECG));
        if(sharedPreferences.getBoolean("collectEMGCheckbox",false))
            tablayout.addTab(tablayout.newTab().setText(R.string.EMG));
        if(sharedPreferences.getBoolean("collectAirflowCheckbox",false))
            tablayout.addTab(tablayout.newTab().setText(R.string.Airflow));
        if(sharedPreferences.getBoolean("collectTemperatureCheckbox",false))
            tablayout.addTab(tablayout.newTab().setText(R.string.Temperature));
        if(sharedPreferences.getBoolean("collectBPCheckbox",false))
            tablayout.addTab(tablayout.newTab().setText(R.string.BP));
        if(sharedPreferences.getBoolean("collectPositionCheckbox",false))
            tablayout.addTab(tablayout.newTab().setText(R.string.Position));
        if(sharedPreferences.getBoolean("collectGSRCheckbox",false))
            tablayout.addTab(tablayout.newTab().setText(R.string.GSR));
        if(sharedPreferences.getBoolean("collectGlucometerCheckbox",false))
            tablayout.addTab(tablayout.newTab().setText(R.string.Glucometer));




        //tablayout.addTab(tablayout.newTab().setText("Tab1"));
        tablayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tablayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapterClass pagerAdapterClass = new PagerAdapterClass(getSupportFragmentManager(),tablayout.getTabCount(),context,moduleOptions);
        viewPager.setAdapter(pagerAdapterClass);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tablayout));
        tablayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //tablayout.removeTabAt(4);

        pagerAdapterClass.notifyDataSetChanged();
    }

    @Override
    protected void onResume(){
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        BluetoothManager manager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        //BluetoothAdapter adapter = manager.getAdapter();
        List<BluetoothDevice> devices = manager.getConnectedDevices(BluetoothProfile.GATT);
        if(devices.size()==0){
            tablayout.setBackgroundColor(Color.RED);
            Toast.makeText(this, R.string.no_devices_connected, Toast.LENGTH_SHORT).show();
            connectionState = false;
        }
        else{
            for(BluetoothDevice device : devices) {
                if(device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                    if(device.getAddress().equals(PreferenceManager.getDefaultSharedPreferences(context).getString(MainActivity.DEVICE_ADDRESS,null))) {
                        Log.i("Device", "connected");
                        connectionState = true;
                    }
                    else
                        tablayout.setBackgroundColor(Color.GRAY);

                }
                else{
                    tablayout.setBackgroundColor(Color.GREEN);

                }

            }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        unregisterReceiver(mGattUpdateReceiver);
    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
    //    getMenuInflater().inflate(R.menu.settings_menu, menu);
    //    return true;
    //}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    private boolean connectionState;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                connectionState = true;
                tablayout.setBackgroundColor(Color.BLUE);
                Toast.makeText(context,R.string.device_refound,Toast.LENGTH_SHORT).show();

            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connectionState = false;
                tablayout.setBackgroundColor(Color.RED);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    @Override
    public boolean connectionState() {
        Log.i("ConnectionState",String.valueOf(connectionState));
        return this.connectionState;
    }
}
