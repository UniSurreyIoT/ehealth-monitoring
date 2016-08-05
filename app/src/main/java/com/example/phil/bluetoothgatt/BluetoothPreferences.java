/**
 * Created by PWS 2015.
 * Preferences for Cell Logger Android Program
 * Setup and display current preferences
 */

package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;


public class BluetoothPreferences extends Activity {


    //Place holders for defaults
    private static final String OPT_AUTO_CONNECT = "autoConnect";
    private static final boolean OPT_AUTO_CONNECT_DEF = false;


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


    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.bluetooth_settings);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            CheckBoxPreference autoConnectCB = (CheckBoxPreference) findPreference("autoConnect");
            autoConnectCB.setChecked(sharedPreferences.getBoolean("autoConnect",true));
            Log.i("SP change","here");
        }

    }

    //Functions to return defaults
    public static boolean getDefaultAutoConnect(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(OPT_AUTO_CONNECT,OPT_AUTO_CONNECT_DEF);
    }
}
