package com.example.phil.bluetoothgatt;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String ADAFRUIT = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";

    public static final String EHEALTHSERVICE =       "00002000-0000-1000-8000-00805f9b34fb";

    public static final String PULSIOXIMETERCHAR =    "00002b00-0000-1000-8000-00805f9b34fb";
    public static final String ECGCHAR =              "00002b01-0000-1000-8000-00805f9b34fb";
    public static final String EMGCHAR =              "00002b02-0000-1000-8000-00805f9b34fb";
    public static final String AIRFLOWCHAR =          "00002b03-0000-1000-8000-00805f9b34fb";
    public static final String TEMPERATURECHAR =      "00002b04-0000-1000-8000-00805f9b34fb";
    public static final String BLOODPRESSURECHAR =    "00002b05-0000-1000-8000-00805f9b34fb";
    public static final String BODYPOSITIONCHAR =     "00002b06-0000-1000-8000-00805f9b34fb";
    public static final String GSRCHAR =              "00002b07-0000-1000-8000-00805f9b34fb";
    public static final String GLUCOMETERCHAR =       "00002b08-0000-1000-8000-00805f9b34fb";
    public static final String EXTRABOARD1 =          "00002b09-0000-1000-8000-00805f9b34fb";


    public static final String PULSIOXIMETERSERVICE = "00002001-0000-1000-8000-00805f9b34fb";
    public static final String SPO2VALUECHAR =        "00002c00-0000-1000-8000-00805f9b34fb";
    public static final String BPMVALUECHAR =         "00002c01-0000-1000-8000-00805f9b34fb";
    public static final String USESPO2CHAR =          "00002c02-0000-1000-8000-00805f9b34fb";
    public static final String USEBPMCHAR =           "00002c03-0000-1000-8000-00805f9b34fb";
    public static String BPMSECONDSCHAR =       "00002c04-0000-1000-8000-00805f9b34fb";
    public static String BPMMINUTESCHAR =       "00002c05-0000-1000-8000-00805f9b34fb";

    public static final String ECGSERVICE =           "00002002-0000-1000-8000-00805f9b34fb";
    public static final String USEECGCHAR =         "00002c10-0000-1000-8000-00805f9b34fb";
    public static final String ECGVALUECHAR =   "00002c11-0000-1000-8000-00805f9b34fb";
    public static final String ECGALPHAVALUECHAR =   "00002c12-0000-1000-8000-00805f9b34fb";
    public static final String ECGOUTPUTTYPECHAR =   "00002c13-0000-1000-8000-00805f9b34fb";


    public static String EMGSERVICE =           "00002002-0000-1000-8000-00805f9b34fb";
    public static final String USEEMGCHAR =           "00002c20-0000-1000-8000-00805f9b34fb";

    public static String AIRFLOWSERVICE =       "00002003-0000-1000-8000-00805f9b34fb";
    public static final String USEAIRFLOWCHAR =       "00002c30-0000-1000-8000-00805f9b34fb";

    public static final String TEMPERATURESERVICE =   "00002004-0000-1000-8000-00805f9b34fb";
    public static final String USETEMPERATURECHAR =   "00002c40-0000-1000-8000-00805f9b34fb";
    public static final String TEMPERATUREVALUECHAR = "00002c41-0000-1000-8000-00805f9b34fb";
    public static final String TEMPERATURESAMPLECHAR = "00002c42-0000-1000-8000-00805f9b34fb";
    public static final String TEMPERATUREPROCESSING = "00002c46-0000-1000-8000-00805f9b34fb";
    public static final String TEMPERATUREMIN = "00002c43-0000-1000-8000-00805f9b34fb";
    public static final String TEMPERATUREMAX = "00002c44-0000-1000-8000-00805f9b34fb";
    public static final String TEMPERATUREMAXSAMPLE = "00002c45-0000-1000-8000-00805f9b34fb";

    public static String BLOODPRESSURESERVICE = "00002005-0000-1000-8000-00805f9b34fb";
    public static final String USEBLOODPRESSURECHAR = "00002c50-0000-1000-8000-00805f9b34fb";

    public static String BODYPOSITIONSERVICE =  "00002006-0000-1000-8000-00805f9b34fb";
    public static final String USEBODYPOSITIONCHAR =  "00002c60-0000-1000-8000-00805f9b34fb";
    public static String BODYPOSITIONVALUECHAR = "00002c61-0000-1000-8000-00805f9b34fb";

    public static String GSRSERVICE =           "00002007-0000-1000-8000-00805f9b34fb";
    public static final String USEGSRCHAR =           "00002c70-0000-1000-8000-00805f9b34fb";
    public static String GSRVALUECHAR =         "00002c71-0000-1000-8000-00805f9b34fb";

    public static String GLUCOMETERSERVICE =    "00002008-0000-1000-8000-00805f9b34fb";
    public static final String USEGLUCOMETERCHAR =    "00002c80-0000-1000-8000-00805f9b34fb";
    public static String GLUECOMETERVALUECHAR = "00002c81-0000-1000-8000-00805f9b34fb";





    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(GLUCOMETERCHAR,"hasGlucometer");
        attributes.put(PULSIOXIMETERCHAR,"hasPusioximeter");
        attributes.put(EMGCHAR,"hasEMG");
        attributes.put(ECGCHAR,"hasECG");
        attributes.put(AIRFLOWCHAR,"hasAirflow");
        attributes.put(TEMPERATURECHAR,"hasTemperature");
        attributes.put(BLOODPRESSURECHAR,"hasBloodPressure");
        attributes.put(BODYPOSITIONCHAR,"hasBodyPosition");
        attributes.put(GSRCHAR,"hasGSR");


    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
