/**
 * Created by Phil on 16/01/2016.
 * Preference for email
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


public class EmailPreferences extends Activity {

    //Place holders for defaults
    private static final String OPT_USE_EMAIL_ADDRESS = "useEmail";
    private static final boolean OPT_USE_EMAIL_ADDRESS_DEF = false;
    private static final String OPT_EMAIL_ADDRESS = "emailAddress";
    private static final String OPT_EMAIL_ADDRESS_DEF = "";



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

    public static class PrefsFragment extends PreferenceFragment {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.email_settings);
        }


    }

    public static boolean getUseDefaultEmailAddress(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(OPT_USE_EMAIL_ADDRESS,OPT_USE_EMAIL_ADDRESS_DEF);
    }

    public static String getDefaultEmailAddress(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OPT_EMAIL_ADDRESS,OPT_EMAIL_ADDRESS_DEF);
    }
}
