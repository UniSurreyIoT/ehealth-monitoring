package com.example.phil.bluetoothgatt;

/**
 * Created by Phil on 15/01/2016.
 * Fragment intended to hold graphical representation of BloodPressure Data
 * Not implemented - dummy placeholder view used
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class BloodPressureFragment extends Fragment implements View.OnClickListener{

    protected Activity mActivity;

    //Create fragment and load layout file
    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle SavedInstanceState){
        View view = inflator.inflate(R.layout.fragment_dummy,container,false);

        TextView textView = (TextView) view.findViewById(R.id.textView);

      textView.setText("BP");
        return view;
    }

    @Override
    public void onClick(View v){

    }

}