/**
 * Created by Phil on 16/01/2016.
 * Main activity presenting users with options to access differenct parts of the program
 */

package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity implements View.OnClickListener {

    public static String DEVICE_ADDRESS = "deviceAddress";
    public static String NO_DEVICE = "No device";
    public static String DEVICE_OPTIONS = "deviceOptions";

    private BluetoothAdapter mBluetoothAdapter;


   // private String connectionAddress = "C4:66:47:A9:45:B8";

    private BluetoothLeService mBluetoothLeService;
    private static final int REQUEST_ENABLE_BT = 1;

    private SharedPreferences sharedPreferences;
    private View connectButton,disconnectButton;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = this.getApplicationContext();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        View graphButton = findViewById(R.id.graph);
        graphButton.setOnClickListener(this);

        View exportDatabase = findViewById(R.id.createCSV);
        exportDatabase.setOnClickListener(this);

        //View connectButton = findViewById(R.id.mainConnect);
        connectButton = findViewById(R.id.mainConnect);
        connectButton.setOnClickListener(this);

        //View disconnectButton = findViewById(R.id.mainDisconnect);
        disconnectButton = findViewById(R.id.mainDisconnect);
        disconnectButton.setOnClickListener(this);
        disconnectButton.setEnabled(false);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.

        View shutdownButton = findViewById(R.id.shutdown);
        shutdownButton.setOnClickListener(this);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Intent bluetoothIntent = new Intent(this,BluetoothLeService.class);
        startService(bluetoothIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Intent bluetoothIntent = new Intent(this,BluetoothLeService.class);
        //startService(bluetoothIntent);
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        startBLEService();

    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if(mBluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED){
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
            }
            else {
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
            }

            if (!mBluetoothLeService.initialize()) {
                Log.e("Service", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            if(BluetoothPreferences.getDefaultAutoConnect(getApplicationContext())) {

                String deviceAddress = sharedPreferences.getString("deviceAddress", "NULL");
                if(!deviceAddress.equals("NULL"))
                    mBluetoothLeService.connect(deviceAddress);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.

    private Dialog dialog;

    private void showDisconnectDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_disconnect);
        Button cancelButton = (Button) dialog.findViewById(R.id.disconnect_cancel_button);

        Button okButton = (Button) dialog.findViewById(R.id.disconnect_ok_button);


        cancelButton.setOnClickListener(this);

        okButton.setOnClickListener(this);

        dialog.show();

    }

    private void showShutdownDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_shutdown);
        Button shutdownButton = (Button) dialog.findViewById(R.id.shutdown_now_button);
        Button backgroundButton = (Button) dialog.findViewById(R.id.shutdown_close_button);
        Button cancelButton = (Button) dialog.findViewById(R.id.shutdown_cancel_button);
        shutdownButton.setOnClickListener(this);
        backgroundButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        dialog.show();
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
                invalidateOptionsMenu();
            }

            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
                invalidateOptionsMenu();
            }

            else if(BluetoothLeService.FINISHED_EHEALTH_DISCOVERY.equals(action)){
                if(connectButtonFlag){
                    mBluetoothLeService.continueDiscovery();
                }
            }

            else if(BluetoothLeService.ACTION_DESCRIPTOR_WRITE.equals(action)){
                if(connectButtonFlag){
                    if(intent.getStringExtra("value").equals(getString(R.string.paediatric_monitor))){
                        String address = sharedPreferences.getString("additionalBoard1Address",null);
                        if(!address.equals(null))
                            mBluetoothLeService.connect2(address);

                    }
                    else if(intent.getStringExtra("value").equals(getString(R.string.paediatric_monitor2))){
                        connectButtonFlag = false;
                        //finish();
                    }
                }
            }
        }
    };





    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.FINISHED_EHEALTH_DISCOVERY);
        intentFilter.addAction(BluetoothLeService.ACTION_DESCRIPTOR_WRITE);

        return intentFilter;
    }


    boolean timeout = false;

    boolean connectButtonFlag = false;

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.graph:
                Intent graphIntent = new Intent (this,ResultsSwipeActivity.class);
                graphIntent.putExtra("Connection_State",mBluetoothLeService.getConnectionState());
                startActivity(graphIntent);
                break;
            case R.id.mainConnect:
                boolean device_available = false;
                String address = sharedPreferences.getString(DEVICE_ADDRESS,null);
                if(address!= null) {
                    device_available = mBluetoothLeService.connect(address);
                    //mBluetoothLeService.setNotifiableCharacteristics();
                }
                if(device_available) {
                    timeout = false;

                    AsyncTask<Void,Void,Void> progress = new ShowProgress(this).execute();

                    final Handler myHandler = new Handler();

                    myHandler.postDelayed(new Runnable() {
                        public void run() {
                            timeout = true;
                        }
                    }
                            , 20000);
                    connectButtonFlag = true;
                }
                else
                    Toast.makeText(this, R.string.device_not_found, Toast.LENGTH_LONG).show();
                //else
                //mBluetoothLeService.connect(connectionAddress);
                break;
            case R.id.mainDisconnect:
                showDisconnectDialog();
                break;
            case R.id.disconnect_ok_button:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("autoConnect",false);
                editor.apply();
                dialog.dismiss();
                mBluetoothLeService.disconnect();
               // mBluetoothLeService.close();
                break;
            case R.id.disconnect_cancel_button:
                dialog.dismiss();
                break;

            case R.id.createCSV:
                Intent exportDatabase = new Intent(this,ExportDatabaseActivity.class);
                startActivity(exportDatabase);
                break;
            case R.id.shutdown_now_button:
                stopService(new Intent(this,BluetoothLeService.class));
                dialog.dismiss();
                finish();
                break;
            case R.id.shutdown_close_button:
                dialog.dismiss();
                finish();
                break;
            case R.id.shutdown_cancel_button:
                dialog.dismiss();
            case R.id.shutdown:
                showShutdownDialog();
                break;
        }
    }

    private void startBLEService(){
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);

        bindService(gattServiceIntent, mServiceConnection, 0);

    }




    //Create Settings menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }


    //Start preferences activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menuBluetoothScan:
                mBluetoothLeService.disconnect();
                startActivity(new Intent(this,BluetoothConnectActivity.class));
                return true;
            case R.id.menuBluetoothSettings:
                startActivity(new Intent(this, BluetoothPreferences.class));
                return true;
            case R.id.menuModuleOptions:
                startActivity(new Intent(this,ModulePreferences.class));
                return true;
            case R.id.clearDatabase:
                DatabaseHelper dbHelper = new DatabaseHelper(this.getApplicationContext());
                dbHelper.clearDatabase();
                return true;
            case R.id.setupEmail:
                startActivity((new Intent(this,EmailPreferences.class)));
                return true;
        }
        return false;
    }


    @Override
    protected void onPause(){

        Log.i("MainActivity","onPause");
        super.onPause();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i("MainActivity","onDestroy");
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
    }

    private class ShowProgress extends AsyncTask<Void,Void,Void> {

        ProgressDialog pd;
        private Context ctx;
        private String message;

        public ShowProgress(Context context) {
            this.ctx = context;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(ctx);

            pd.setTitle(R.string.collectingInformation);

            pd.setMessage(getString(R.string.pleaseWait));
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (connectButtonFlag && !timeout) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (pd != null) {
                pd.dismiss();
            }
        }

    }

}
