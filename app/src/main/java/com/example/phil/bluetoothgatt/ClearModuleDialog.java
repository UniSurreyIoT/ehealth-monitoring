package com.example.phil.bluetoothgatt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Phil on 16/01/2016.
 * Simple dialog to clear module details
 */

public class ClearModuleDialog extends DialogPreference{

    Context clearModuleContext;

    public ClearModuleDialog(Context context, AttributeSet attrs) {
        super(context, attrs);

        clearModuleContext = context;
        setPositiveButtonText("Clear module");
    }


    @Override
    protected View onCreateDialogView() {


        LinearLayout layout = new LinearLayout(clearModuleContext);

        layout.setPadding(6,6,6,6);
        return layout;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(clearModuleContext);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("ModuleDefault",true);
            editor.putBoolean("autoConnect",false);
            editor.putString(MainActivity.DEVICE_ADDRESS, MainActivity.NO_DEVICE);
            editor.putBoolean("ModuleSelected",false);

            editor.apply();


        }
    }


}






