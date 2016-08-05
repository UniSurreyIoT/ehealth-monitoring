/**
 * Created by Phil on 16/01/2016.
 * Fragment intended to hold graphical representation of Glucometer data
 * Not implemented at present - placeholder graphics used
 */

package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class GlucometerFragment extends Fragment implements View.OnClickListener{

    protected Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle SavedInstanceState){
        View view = inflator.inflate(R.layout.fragment_dummy,container,false);

        TextView textView = (TextView) view.findViewById(R.id.textView);

        textView.setText("Glucometer");
        return view;
    }

    @Override
    public void onClick(View v){

    }

}