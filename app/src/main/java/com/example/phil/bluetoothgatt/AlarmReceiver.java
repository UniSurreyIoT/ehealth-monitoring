package com.example.phil.bluetoothgatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

/**
 * Created by PWS in 2016.
 * This class handles the automatic reconnect of the eHealth device
 */
public class AlarmReceiver extends BroadcastReceiver {

    public final static String ACTION_DEVICE_REFOUND = "DEVICE_REFOUND";
    public final static String CHECK_ALARM_CANCELED = "CHECK CANCELED";

    private Context appContext;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private int SCAN_PERIOD = 10000;
    private String connectionAddress = "C4:66:47:A9:45:B8";

    //Setup what to do on intent received
    @Override
    public void onReceive(Context context, Intent intent) {

        appContext = context;
        mHandler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter.isEnabled()) {

           scanSpecificDevice();

        }
    }

    private void scanSpecificDevice(){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                }
            }, SCAN_PERIOD);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
     }




    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if(device.getAddress().equals(connectionAddress)){

                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        Log.i("Alarm", "Found it again");

                        final Intent intent = new Intent(ACTION_DEVICE_REFOUND);
                        appContext.sendBroadcast(intent);
                        }
                    }
                };


}
