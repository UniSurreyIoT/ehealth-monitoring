/**
 * Created by PWS in 2016.
 * Activity to allow display and selection of eHealth device
 */

package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;



public class BluetoothConnectActivity extends ListActivity  implements View.OnClickListener {

    private boolean debug = false;

    private Handler mHandler;

    private static final long SCAN_PERIOD = 10000;

    private static final int REQUEST_ENABLE_BT=1;

    private int SENSOR_DISCOVERY = 0;
    private int CHARACTERISTIC_DISCOVERY = 1;

    private String deviceAddress;

    private boolean mScanning;
    private boolean characteristicDiscovery = false;
    private boolean timeout = false;

    private Context context;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeService mBluetoothLeService;

    private LeDeviceListAdapter mLeDeviceListAdapter;

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    private Dialog dialog;

    Context activityContext;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        activityContext = this.getBaseContext();
        context = getApplicationContext();
        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
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

        //Sets up Broadcast receiver and binds to service
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);

        //Scan for devices
        scanLeDevice(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //No menu, but method shown for completeness
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
        }

    //View.onClick for pop up buttons
    public void onClick(View v){
        switch (v.getId()){
            //Store device and and continue to discover services
            case R.id.popup_select_button:
                clearCurrentSettings();
                editor = sharedPreferences.edit();
                editor.putString(MainActivity.DEVICE_ADDRESS,deviceAddress);
                editor.apply();
                dialog.dismiss();
                characteristicDiscovery = true;
                showProgressDialog(getString(R.string.continuing_discovery),CHARACTERISTIC_DISCOVERY);
                mBluetoothLeService.continueDiscovery();
                break;
            case R.id.popup_cancel_button:
                dialog.dismiss();
                mBluetoothLeService.disconnect();
                break;

        }
    }

    //Method for list item selected
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);

        if (device == null) return;

        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }

        //Make sure now devices connected
        mBluetoothLeService.disconnect();

        mBluetoothLeService.connect(device.getAddress());

        deviceAddress = device.getAddress();

        mBluetoothLeService.setFinishedDiscovery(false);

        //Let user know application is doing something
        showProgressDialog(getString(R.string.available_sensors),SENSOR_DISCOVERY);
    }

    //Show progress dialog but also use a timer to protect against looping
    private void showProgressDialog(String header, int task){
        timeout=false;

        AsyncTask<Void,Void,Void> progress = new ShowProgress(this,header,task).execute();

        final Handler myHandler = new Handler();

        myHandler.postDelayed(new Runnable() {
            public void run() {
                timeout = true;
            }
        }
                ,10000);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    //Connect to BluetoothLeService
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                Log.e("Service", "Unable to initialize Bluetooth");
                finish();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = BluetoothConnectActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            Log.i("Device name", device.getName());
            if(!mLeDevices.contains(device) && !device.getName().equals("Paediatric Monitor2") ) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

       @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Unknown");
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                });
            }
    };

    //Callback to listen for result of user not activating Bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                if(debug)
                    Log.i("MGatt","disconnected");
                timeout = true;
                Toast.makeText(context,"Discovery failed - please try again",Toast.LENGTH_SHORT).show();

            }

            //Read available services and characteristics
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                editor = sharedPreferences.edit();
                if(debug)
                    Log.i("Data","fired");

                if(intent.hasExtra("parentService")) {
                    if(debug)
                        Log.i("Data","hasparent");

                    String parentService = intent.getStringExtra("parentService");

                    if(debug)
                        Log.i("ParentService",parentService);

                    //Store available sensors to defaultPreferences
                    if (parentService.equals(GattAttributes.EHEALTHSERVICE)) {
                        if(debug)
                            Log.i("Broadcast","eHealth");
                        String preferenceString = null;

                        switch(intent.getStringExtra("characteristic")) {
                            case GattAttributes.PULSIOXIMETERCHAR:
                                preferenceString = "hasPulsioximeter";
                                break;
                            case GattAttributes.ECGCHAR:
                                preferenceString = "hasECG";
                                break;
                            case GattAttributes.EMGCHAR:
                                preferenceString = "hasEMG";
                                break;
                            case GattAttributes.AIRFLOWCHAR:
                                preferenceString = "hasAirflow";
                                break;
                            case GattAttributes.TEMPERATURECHAR:
                                preferenceString = "hasTemperature";
                                break;
                            case GattAttributes.BLOODPRESSURECHAR:
                                preferenceString = "hasBloodPressure";
                                break;
                            case GattAttributes.BODYPOSITIONCHAR:
                                preferenceString = "hasBodyPosition";
                                break;
                            case GattAttributes.GSRCHAR:
                                preferenceString = "hasGSR";
                                break;
                            case GattAttributes.GLUCOMETERCHAR:
                                preferenceString = "hasGlucometer";
                                break;
                            case GattAttributes.EXTRABOARD1:
                                preferenceString = "additionalBoard1Address";
                            default:
                                break;
                        }

                        if(preferenceString != null) {
                            if(!preferenceString.equals("additionalBoard1Address")) {
                                editor.putBoolean(preferenceString, intent.getBooleanExtra("valueBoolean", false));

                            }
                            else{
                                editor.putString(preferenceString,intent.getStringExtra("value"));

                            }
                            editor.apply();
                        }
                    }

                    //Store values from each of the sensor services
                    else if(parentService.equals(GattAttributes.PULSIOXIMETERSERVICE)){
                        boolean setting;
                        setting = intent.getBooleanExtra("valueBoolean",false);
                        if(debug)
                            Log.i("Setting",Boolean.toString(setting));
                        switch(intent.getStringExtra("characteristic")){
                            case GattAttributes.USEBPMCHAR:
                                if(setting){

                                }

                                editor.putBoolean("collectBPMCheckbox",setting);

                                break;
                            case GattAttributes.USESPO2CHAR:
                                if(setting){

                                }

                                editor.putBoolean("collectSPO2Checkbox",setting);
                                break;
                            default:
                                break;

                        }
                        editor.apply();
                    }

                    else if(parentService.equals(GattAttributes.ECGSERVICE)){
                        boolean setting;
                        setting = intent.getBooleanExtra("valueBoolean",false);
                        switch(intent.getStringExtra("characteristic")){
                            case GattAttributes.USEECGCHAR:
                                if(setting){

                                }
                                editor.putBoolean("collectECGCheckbox",true);
                                break;
                            default:
                                break;

                        }
                        editor.apply();
                    }

                    else if(parentService.equals(GattAttributes.EMGSERVICE)){
                        boolean setting;
                        setting = intent.getBooleanExtra("valueBoolean",false);
                        switch(intent.getStringExtra("characteristic")){
                            case GattAttributes.USEEMGCHAR:
                                if(setting){

                                }

                                editor.putBoolean("collectEMGCheckbox",setting);

                                break;
                            default:
                                break;

                        }
                        editor.apply();
                    }

                    else if(parentService.equals(GattAttributes.AIRFLOWSERVICE)){
                        boolean setting;
                        setting = intent.getBooleanExtra("valueBoolean",false);
                        switch(intent.getStringExtra("characteristic")){
                            case GattAttributes.USEAIRFLOWCHAR:
                                if(setting){

                                }

                                editor.putBoolean("collectAirflowCheckbox",setting);

                                break;
                            default:
                                break;

                        }
                        editor.apply();
                    }

                    else if(parentService.equals(GattAttributes.TEMPERATURESERVICE)){
                        boolean setting;
                        setting = intent.getBooleanExtra("valueBoolean",false);
                        switch(intent.getStringExtra("characteristic")){
                            case GattAttributes.USETEMPERATURECHAR:
                                if(setting){

                                }

                                editor.putBoolean("collectTemperatureCheckbox",setting);

                                break;

                            default:
                                break;

                        }
                        editor.apply();
                    }

                    else if(parentService.equals(GattAttributes.BODYPOSITIONSERVICE)){
                        boolean setting;
                        setting = intent.getBooleanExtra("valueBoolean",false);
                        switch(intent.getStringExtra("characteristic")){
                            case GattAttributes.USEBODYPOSITIONCHAR:
                                if(setting){

                                }

                                editor.putBoolean("collectBodyPositionCheckbox",setting);

                                break;
                            default:
                                break;

                        }
                        editor.apply();
                    }

                    else if(parentService.equals(GattAttributes.GSRSERVICE)){
                        boolean setting;
                        setting = intent.getBooleanExtra("valueBoolean",false);
                        switch(intent.getStringExtra("characteristic")){
                            case GattAttributes.USEGSRCHAR:
                                if(setting){

                                }

                                editor.putBoolean("collectGSRCheckbox",setting);

                                break;
                            default:
                                break;

                        }
                        editor.apply();
                    }

                    else if(parentService.equals(GattAttributes.GLUCOMETERSERVICE)){
                        boolean setting;
                        setting = intent.getBooleanExtra("valueBoolean",false);
                        switch(intent.getStringExtra("characteristic")){
                            case GattAttributes.USEGLUCOMETERCHAR:
                                if(setting){

                                }

                                editor.putBoolean("collectGlucometerCheckbox",setting);

                                break;
                            default:
                                break;

                        }
                        editor.apply();
                    }

                    else if(parentService.equals(GattAttributes.BLOODPRESSURESERVICE)){
                        boolean setting;
                        setting = intent.getBooleanExtra("valueBoolean",false);
                        switch(intent.getStringExtra("characteristic")){
                            case GattAttributes.USEBLOODPRESSURECHAR:
                                if(setting){

                                }

                                editor.putBoolean("collectBPMCheckbox",setting);

                                break;
                            default:
                                break;

                        }
                        editor.apply();
                    }


                }
            }

            //Broadcast receiver for showing sensor availability
            else if(action.equals(BluetoothLeService.FINISHED_EHEALTH_DISCOVERY)){
                if(debug)
                    Log.i("FINISHED","EHEALTH");
                initiateOptionsDialog();
                timeout = true;
            }

            //Broadcast receiver for completion of each board discovery
            else if (BluetoothLeService.ACTION_DESCRIPTOR_WRITE.equals(action)){

                if(debug)
                    Log.i("BluetoothConnect","ACTION__WRITE");
                Log.i("ACTION_DESCRIPTOR",intent.getStringExtra("value"));
                if((intent.getStringExtra("value")).equals(getString(R.string.paediatric_monitor))){
                    String address = sharedPreferences.getString("additionalBoard1Address",null);
                    Log.i("Board2 address",address);
                    if(address != null)
                        mBluetoothLeService.connect2(address);
                }
                else if((intent.getStringExtra("value")).equals(getString(R.string.paediatric_monitor2))){
                    Log.i("Test","Address");
                    characteristicDiscovery = false;
                    finish();
                }

            }


            else if(BluetoothLeService.NO_EHEALTH_SERVICE.equals(action)){
                Log.i("Action","no ehealth");
            }

        }
    };

    //Setup of filters for wanted Broadcasts
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DESCRIPTOR_WRITE);
        intentFilter.addAction(BluetoothLeService.FINISHED_EHEALTH_DISCOVERY);
        intentFilter.addAction(BluetoothLeService.NO_EHEALTH_SERVICE);
        return intentFilter;
    }

    //Dialog for sensor availability
    private void initiateOptionsDialog(){
        try {
            boolean defaultSetting = false;
            dialog = new Dialog(this);
            dialog.setTitle(R.string.sensorAvailability);
            dialog.setContentView(R.layout.dialog_moduledetails);

            CheckBox SPO2Checkbox = (CheckBox) dialog.findViewById(R.id.popup_SPO2);
            CheckBox BPMCheckbox = (CheckBox) dialog.findViewById(R.id.popup_BPM);
            CheckBox ECGCheckbox = (CheckBox) dialog.findViewById(R.id.popup_ECG);
            CheckBox EMGCheckbox = (CheckBox) dialog.findViewById(R.id.popup_EMG);
            CheckBox AirflowCheckbox = (CheckBox) dialog.findViewById(R.id.popup_airflow);
            CheckBox TemperatureCheckbox = (CheckBox) dialog.findViewById(R.id.popup_temperature);
            CheckBox BloodPressureCheckbox = (CheckBox) dialog.findViewById(R.id.popup_bloodpressure);
            CheckBox BodyPositionCheckbox = (CheckBox) dialog.findViewById(R.id.popup_bodyposition);
            CheckBox GSRCheckbox = (CheckBox) dialog.findViewById(R.id.popup_GSR);
            CheckBox GlucometerCheckbox = (CheckBox) dialog.findViewById(R.id.popup_glucometer);

            SPO2Checkbox.setChecked(sharedPreferences.getBoolean("hasPulsioximeter",defaultSetting));
            BPMCheckbox.setChecked(sharedPreferences.getBoolean("hasPulsioximeter",defaultSetting));
            ECGCheckbox.setChecked(sharedPreferences.getBoolean("hasECG",defaultSetting));
            EMGCheckbox.setChecked(sharedPreferences.getBoolean("hasEMG",defaultSetting));
            AirflowCheckbox.setChecked(sharedPreferences.getBoolean("hasAirflow",defaultSetting));
            TemperatureCheckbox.setChecked(sharedPreferences.getBoolean("hasTemperature",defaultSetting));
            BloodPressureCheckbox.setChecked(sharedPreferences.getBoolean("hasBloodPressure",defaultSetting));
            BodyPositionCheckbox.setChecked(sharedPreferences.getBoolean("hasBodyPosition",defaultSetting));
            GSRCheckbox.setChecked(sharedPreferences.getBoolean("hasGSR",defaultSetting));
            GlucometerCheckbox.setChecked(sharedPreferences.getBoolean("hasGlucometer",defaultSetting));

            Button cancelButton = (Button) dialog.findViewById(R.id.popup_cancel_button);
            Button saveButton = (Button) dialog.findViewById(R.id.popup_select_button);

            cancelButton.setOnClickListener(this);
            saveButton.setOnClickListener(this);

            dialog.show();
        }
        catch(Exception ex){

        }
    }

    //Clear the current preferences
    private void clearCurrentSettings(){
        editor = sharedPreferences.edit();
        editor.putBoolean("collectBPMCheckbox",false);
        editor.putBoolean("collectSPO2Checkbox",false);
        editor.putBoolean("collectEMGCheckbox",false);
        editor.putBoolean("collectECGCheckbox",false);
        editor.putBoolean("collectAirflowCheckbox",false);
        editor.putBoolean("collectTemperatureCheckbox",false);
        editor.putBoolean("collectBodyPositionCheckbox",false);
        editor.putBoolean("collectBloodPressureCheckbox",false);
        editor.putBoolean("collectGSRCheckbox",false);
        editor.putBoolean("collectGlucometerCheckbox",false);
        editor.apply();
    }

    //Close service binding on exit
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        if(dialog!=null){
            dialog.dismiss();
        }
    }

    //Progress dialog class
    public class ShowProgress extends AsyncTask<Void,Void,Void> {

        ProgressDialog pd;
        private Context ctx;
        private String message;
        private int task;

        public ShowProgress(Context context, String message, int task) {
            this.ctx = context;
            this.message = message;
            this.task = task;
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
            if(task == SENSOR_DISCOVERY) {

                while (!mBluetoothLeService.getFinishedDiscovery() && !timeout) {

                }
            }
            else if(task == CHARACTERISTIC_DISCOVERY){
                while(characteristicDiscovery && !timeout){

                }
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
