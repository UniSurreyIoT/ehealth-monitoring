/**
 * Created by Phil on 16/01/2016.
 * Class to hold details about a reading
 *
 * */

package com.example.phil.bluetoothgatt;

public class ReadingClass {



    //private variables
    long mTime;
    int mValueInt;
    String mType;
    String mValueString;

    // Empty constructor
    public ReadingClass(){
        this.mTime = -1;
        this.mValueInt = -1;
        this.mType = "";
        this.mValueString = "";
    }
    // constructor
    public ReadingClass(long time, int value, String type){
        this.mTime = time;
        this.mValueInt = value;
        this.mType = type;
    }

    public ReadingClass(long time, String value, String type){
        this.mTime = time;
        this.mValueString = value;
        this.mType = type;
    }

    // getting Time
    public long getTime(){
        return this.mTime;
    }

    // setting Time
    public void setTime(long time){
        this.mTime = time;
    }

    // getting Integer Value
    public int getValueInt(){
        return this.mValueInt;
        }

    // setting Integer Value
    public void setValueInt(int value){
        this.mValueInt = value;
    }

    // getting type
    public String getType(){
        return this.mType;
    }

    // setting type
    public void setType(String type){
        this.mType = type;
    }

    // getting string value
    public String getValueString(){
        return this.mValueString;
    }

    //setting string value
    public void setValueString(String value){
        this.mValueString = value;
    }

}
