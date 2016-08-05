package com.example.phil.bluetoothgatt;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Phil on 16/01/2016.
 * Fragment intended to hold graphical representation of Airflow data
 * Not implemented at present - placeholder graphics used
 */

public class BodyPositionFragment extends Fragment implements View.OnClickListener{

    protected Activity mActivity;


    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle SavedInstanceState){
        View view = inflator.inflate(R.layout.fragment_dummy,container,false);

        TextView textView = (TextView) view.findViewById(R.id.textView);

        textView.setText("Position");
        return view;
    }

    @Override
    public void onClick(View v){

    }

}