/**
 * Created by Phil on 16/01/2016.
 * Activity to store preferences for Temperature collection
 */
package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;


public class TemperaturePreferences extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();

    }

    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        ListPreference noCompression ;
        EditTextPreference maxValue,minValue;
        TimeSelector timeSelector;
        PreferenceCategory adaptive;
        SharedPreferences sharedPreferences;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.temperature_settings);
            noCompression = (ListPreference)findPreference("TemperatureProcessing");
            noCompression.setOnPreferenceChangeListener(this);
            maxValue = (EditTextPreference)findPreference("temperatureAdaptiveMinimum");
            maxValue.setOnPreferenceChangeListener(this);
            minValue = (EditTextPreference)findPreference("temperatureAdaptiveMaximum");
            minValue.setOnPreferenceChangeListener(this);
            timeSelector = (TimeSelector)findPreference("temperatureMaximumInterval");
            timeSelector.setOnPreferenceChangeListener(this);
            adaptive = (PreferenceCategory)findPreference("Adaptive compression");
            sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            String processing = sharedPreferences.getString("TemperatureProcessing","1");
            Log.i("Processing",processing);
            if(!processing.equals("4")){
                adaptive.setEnabled(false);
            }
            else
                adaptive.setEnabled(true);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            int value = 0;
            switch(preference.getKey()){
                case("TemperatureProcessing"):
                    if(newValue.toString().equals("1")){
                        adaptive.setEnabled(false);
                        value = 1;
                    }
                    else if(newValue.toString().equals("2")){
                        adaptive.setEnabled(false);
                        value = 2;
                    }
                    else if(newValue.toString().equals("3")){
                        adaptive.setEnabled(false);
                        value = 3;
                    }
                    else if(newValue.toString().equals("4")){
                        adaptive.setEnabled(true);
                        value = 4;
                    }
                    if(value != 0)
                        broadcastUpdate(GattAttributes.TEMPERATUREPROCESSING, value, 1);
                    break;
                case("temperatureAdaptiveMinimum"):
                    float f = Float.valueOf(newValue.toString());
                    Log.i("Minimum",String.valueOf(f));
                    float max = Float.valueOf(sharedPreferences.getString("temperatureAdaptiveMaximum","0.0"));
                    Log.i("Maximum",String.valueOf(max));
                    int adaptiveValue = (int) (f*100);
                    if(f < max) {

                        broadcastUpdate(GattAttributes.TEMPERATUREMIN, adaptiveValue, 0);
                    }
                    else {
                        PreferenceScreen screen = (PreferenceScreen) findPreference("temperatureProcessingScreen");

                        int pos = findPreference("temperatureAdaptiveMaximum").getOrder();
                        screen.onItemClick(null, null, pos, 0);
                    }
                    break;
                case("temperatureAdaptiveMaximum"):
                    f = Float.valueOf(newValue.toString());
                    adaptiveValue = (int) (f*100);
                    broadcastUpdate(GattAttributes.TEMPERATUREMAX,adaptiveValue,0);
                    break;

                case("temperatureMaximumInterval"):
                    //characteristic = mBluetoothLeService.setCharacteristic(GattAttributes.TEMPERATURESERVICE,GattAttributes.TEMPERATUREMAXSAMPLE);
                    broadcastUpdate(GattAttributes.TEMPERATUREMAXSAMPLE, (int) newValue, 4);
                    break;
            }
            return true;
        }

        private void broadcastUpdate(String characteristicString, final int value,int type) {
            final Intent intent = new Intent(BluetoothLeService.CHARACTERISTIC_WRITE);
            intent.putExtra("parentService", GattAttributes.TEMPERATURESERVICE);

            intent.putExtra("characteristic", characteristicString);

            intent.putExtra("value",value);

            intent.putExtra("type",type);

            Log.i("Broadcast", "sent");
            getActivity().sendBroadcast(intent);
        }
    }


}
