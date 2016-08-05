/**
 * Created by Phil on 16/01/2016.
 * Holder class to take values of characteristics to be written
 */
package com.example.phil.bluetoothgatt;

import android.bluetooth.BluetoothGattCharacteristic;




public class WriteCharacteristics {
    private BluetoothGattCharacteristic characteristic;
    private int intValue;
    private String stringValue;
    private int type;

    public WriteCharacteristics(BluetoothGattCharacteristic c, int i){
        this.characteristic = c;
        this.intValue = i;
        this.type = 0;
    }

    public WriteCharacteristics(BluetoothGattCharacteristic c, String s){
        this.characteristic = c;
        this.stringValue = s;
        this.type = 1;
    }

    public int getType(){
        return this.type;

    }

    public void setType(int i){
        this.type = i;
    }
    public BluetoothGattCharacteristic getCharacteristic(){
        return characteristic;
    }

    public String getStringValue(){
        return this.stringValue;
    }

    public int getIntValue(){
        return this.intValue;
    }
}
