/**
 * Created by Phil on 16/01/2016.
 * Class to organise formatting of data values for display on graph
*/

package com.example.phil.bluetoothgatt;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;


public class GraphValueFormatter implements ValueFormatter {

    private DecimalFormat mFormat;

    public GraphValueFormatter() {
        mFormat = new DecimalFormat("###,###,###.##"); // use one decimal


    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        // write your logic here
        return mFormat.format(value); // e.g. append a dollar-sign
    }
}