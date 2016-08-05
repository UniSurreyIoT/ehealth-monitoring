/**
 * Created by Phil on 16/01/2016.
 * Class to select which fragment to show in tab page
 */


package com.example.phil.bluetoothgatt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;


public class PagerAdapterClass extends FragmentStatePagerAdapter {

    int mNumOfTabs;
    Context context;
    int moduleOptions;

    public PagerAdapterClass(FragmentManager fm, int NumOfTabs,Context appContext,int moduleOptionsValue){
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.context = appContext;
        moduleOptions = moduleOptionsValue;
    }

    @Override
    public Fragment getItem(int position){
        boolean defaultValue = false;
        Log.i("Position",String.valueOf(position));
        int testPosition = -1;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(sharedPreferences.getBoolean("collectSPO2Checkbox",defaultValue)){
            testPosition++;
            if(testPosition == position)
                return new SPO2Fragment();
        }
        if(sharedPreferences.getBoolean("collectBPMCheckbox",defaultValue)){
            testPosition++;
            if(testPosition == position)
                return new BPMFragment();
        }
        if(sharedPreferences.getBoolean("collectECGCheckbox",defaultValue)){
            testPosition++;
            if(testPosition == position)
                return new ECGFragment();
        }
        if(sharedPreferences.getBoolean("collectEMGCheckbox",defaultValue)){
            testPosition++;
            if(testPosition == position)
                return new EMGFragment();
        }
        if(sharedPreferences.getBoolean("collectAirflowCheckbox",defaultValue)){
            testPosition++;
            if(testPosition == position)
                return new AirflowFragment();
        }
        if(sharedPreferences.getBoolean("collectTemperatureCheckbox",defaultValue)){
            testPosition++;
            if(testPosition == position)
                return new TemperatureFragment();
        }

        if(sharedPreferences.getBoolean("collectBPCheckbox",defaultValue)){
            testPosition++;
            if(testPosition == position)
                return new BloodPressureFragment();
        }

        if(sharedPreferences.getBoolean("collectPositionCheckbox",defaultValue)){
            testPosition++;
            if(testPosition == position)
                return new BodyPositionFragment();
        }
        if(sharedPreferences.getBoolean("collectGSRCheckbox",defaultValue)){
            testPosition++;
            if(testPosition == position)
                return new GSRFragment();
        }
        if(sharedPreferences.getBoolean("collectGlucometerCheckbox",defaultValue)){
            testPosition++;
            if(testPosition == position)
                return new GlucometerFragment();
        }
        return null;
    }

    @Override
    public int getCount(){
        return mNumOfTabs;
    }

    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }



}
