/**
 * Created by PWS in 2016.
 * Class to implement formatting for y values on graph
 */

package com.example.phil.bluetoothgatt;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.DecimalFormat;


public class YValueFormatter implements YAxisValueFormatter {


    private DecimalFormat mFormat;

    public YValueFormatter () {
        mFormat = new DecimalFormat("###,###,###.##"); // use one decimal
    }

    @Override
    public String getFormattedValue(float value, YAxis yAxis) {
        // write your logic here
        // access the YAxis object to get more information
        return mFormat.format(value); // e.g. append a dollar-sign
    }
}

