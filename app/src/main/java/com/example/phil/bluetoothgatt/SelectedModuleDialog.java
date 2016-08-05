/**
 * Created by Phil on 16/01/2016.
 * Dialog to disaply module details
 */

package com.example.phil.bluetoothgatt;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class SelectedModuleDialog extends DialogPreference {

    private Context moduleContext;

    public SelectedModuleDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText(null);
        moduleContext = context;
    }


    @Override
    protected View onCreateDialogView() {

        LayoutInflater inflater = (LayoutInflater) moduleContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_selectedmodule,null);
        TextView textView = (TextView) layout.findViewById(R.id.moduleAddress);
        textView.setText(PreferenceManager.getDefaultSharedPreferences(moduleContext).getString(MainActivity.DEVICE_ADDRESS,"Can't retrieve module address"));
        return layout;
    }
}
