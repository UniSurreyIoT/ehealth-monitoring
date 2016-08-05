/**
 * Created by Phil on 16/01/2016.
 * Activity to allow settings to be stored relating to the hardware module
 */

package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;


public class ModulePreferences extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static Context context;
    private static boolean settingsChanged;

    //Place holders for defaults
    private static final String OPT_AUTO_CONNECT = "autoConnect";
    private static final boolean OPT_AUTO_CONNECT_DEF = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();


        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();





    }
    private BluetoothLeService mBluetoothLeService;

    @Override
    protected void onResume(){
        super.onResume();
        startBLEService();
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }

    //Allow connection to BluetoothLEService
    private void startBLEService(){
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

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



    public static class PrefsFragment extends PreferenceFragment {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.module_settings);



        }



        @Override
        public void onResume(){
            super.onResume();


            // Load the preferences from an XML resource and remove if not available on selected hardware module
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            PreferenceScreen preferenceScreen = this.getPreferenceScreen();

            if(!preferences.getBoolean("hasPulsioximeter",false)) {
                preferenceScreen.removePreference(preferenceScreen.findPreference("collectSPO2Checkbox"));
            }

            if(!preferences.getBoolean("hasPulsioximeter",false)) {
                preferenceScreen.removePreference(preferenceScreen.findPreference("collectBPMCheckbox"));
            }

            if(!preferences.getBoolean("hasECG",false)) {
                preferenceScreen.removePreference(preferenceScreen.findPreference("collectECGCheckbox"));
            }

            if(!preferences.getBoolean("hasEMG",false)) {
                preferenceScreen.removePreference(preferenceScreen.findPreference("collectEMGCheckbox"));
            }

            if(!preferences.getBoolean("hasAirflow",false)) {
                preferenceScreen.removePreference(preferenceScreen.findPreference("collectAirflowCheckbox"));
            }

            if(!preferences.getBoolean("hasTemperature",false)) {
                preferenceScreen.removePreference(preferenceScreen.findPreference("collectTemperatureCheckbox"));
            }

            if(!preferences.getBoolean("hasBloodPressure",false)) {
                preferenceScreen.removePreference(preferenceScreen.findPreference("collectBPCheckbox"));
            }

            if(!preferences.getBoolean("hasBodyPosition",false)) {
                preferenceScreen.removePreference(preferenceScreen.findPreference("collectPositionCheckbox"));
            }

            if(!preferences.getBoolean("hasGSR",false)) {
                preferenceScreen.removePreference(preferenceScreen.findPreference("collectGSRCheckbox"));
            }

            if(!preferences.getBoolean("hasGlucometer",false)) {
                preferenceScreen.removePreference(preferenceScreen.findPreference("collectGlucometerCheckbox"));
            }

        }

    }


    //Do something if a setting has been changed
    //Update hardware module with collection details

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        BluetoothGattCharacteristic characteristic = null;
        int value = 0;
        //Log.i("key",key);

        settingsChanged = true;

        if (key.equals("collectSPO2Checkbox")){
            characteristic = mBluetoothLeService.setCharacteristic(GattAttributes.PULSIOXIMETERSERVICE, GattAttributes.USESPO2CHAR);
            Log.i("Here","now");
            if(sharedPreferences.getBoolean(key,false))
                value = 1;
            else
                value = 0;
        }

        else if (key.equals("collectBPMCheckbox")){
            characteristic = mBluetoothLeService.setCharacteristic(GattAttributes.PULSIOXIMETERSERVICE, GattAttributes.USEBPMCHAR);
            if(sharedPreferences.getBoolean(key,false))
                value = 1;
            else
                value = 0;
        }

        else if (key.equals("collectECGCheckbox")){
        //Don't want to send anything from here at the moment to save bandwidth
        }

        else if (key.equals("collectEMGCheckbox")){
            characteristic = mBluetoothLeService.setCharacteristic(GattAttributes.EMGSERVICE, GattAttributes.USEEMGCHAR);

            if(sharedPreferences.getBoolean(key,false))
                value = 1;
            else
                value = 0;
        }

        else if (key.equals("collectAirflowCheckbox")){
            characteristic = mBluetoothLeService.setCharacteristic(GattAttributes.AIRFLOWSERVICE, GattAttributes.USEAIRFLOWCHAR);

            if(sharedPreferences.getBoolean(key,false))
                value = 1;
            else
                value = 0;
        }

        else if (key.equals("collectTemperatureCheckbox")) {
            characteristic = mBluetoothLeService.setCharacteristic(GattAttributes.TEMPERATURESERVICE, GattAttributes.USETEMPERATURECHAR);
            if(sharedPreferences.getBoolean(key,false))
                value = 1;
            else
                value = 0;
        }

        else if (key.equals("collectBPCheckbox")){
            characteristic = mBluetoothLeService.setCharacteristic(GattAttributes.BLOODPRESSURESERVICE, GattAttributes.USEBLOODPRESSURECHAR);

            if(sharedPreferences.getBoolean(key,false))
                value = 1;
            else
                value = 0;
        }

        else if (key.equals("collectPositionCheckbox")){
            characteristic = mBluetoothLeService.setCharacteristic(GattAttributes.BODYPOSITIONSERVICE, GattAttributes.USEBODYPOSITIONCHAR);

            if(sharedPreferences.getBoolean(key,false))
                value = 1;
            else
                value = 0;
        }

        else if (key.equals("collectGSRCheckbox")){
            characteristic = mBluetoothLeService.setCharacteristic(GattAttributes.GSRSERVICE, GattAttributes.USEGSRCHAR);

            if(sharedPreferences.getBoolean(key,false))
                value = 1;
            else
                value = 0;
        }

        else if (key.equals("collectGlucometerCheckbox")){

            characteristic = mBluetoothLeService.setCharacteristic(GattAttributes.GLUCOMETERSERVICE, GattAttributes.USEGLUCOMETERCHAR);

            if(sharedPreferences.getBoolean(key,false))
                value = 1;
            else
                value = 0;
        }

        if(characteristic != null) {
            WriteCharacteristics updateSettings = new WriteCharacteristics(characteristic, value);
            mBluetoothLeService.writeCharacteristic(updateSettings);
        }

    }

    @Override
    public void onPause(){
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(settingsChanged){
            sendNewSettings();
        }
    }

    protected void sendNewSettings(){
        final Intent intent = new Intent("Update Module");
        sendBroadcast(intent);
    }
}
