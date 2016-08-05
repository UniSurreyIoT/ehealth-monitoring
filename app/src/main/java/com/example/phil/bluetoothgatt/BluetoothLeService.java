/**
 * Created 201616 by PWS
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */

package com.example.phil.bluetoothgatt;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BluetoothLeService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    boolean DEBUG = false;

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private Context context;

    private BluetoothManager mBluetoothManager;

    private BluetoothAdapter mBluetoothAdapter;

    private String mBluetoothDeviceAddress;
    private String mainBoardAddress,subBoard1Address;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGatt mBluetoothGatt2;

    private int mConnectionState = STATE_DISCONNECTED;
    private int mConnectionStateSubBoard = STATE_DISCONNECTED;

    private long interval = 1;

    private static int serviceLocation;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private DatabaseHelper db;

    private boolean finishedSetNotify = false;
    private boolean finishedDiscovery = false;
    private boolean readFlag;
    private boolean writeInProgress;
    private boolean serviceReconnecting = false;



    private Queue<BluetoothGattCharacteristic> readQueue = new ConcurrentLinkedQueue<BluetoothGattCharacteristic>();
    private Queue<BluetoothGattCharacteristic> notifiableCharacteristics = new LinkedList<>();
    private Queue<WriteCharacteristics> writeQueue = new LinkedList<>();

    private SharedPreferences sharedPreferences;

    private static boolean sensorFlag = false;
    private static boolean running;
    private static boolean ehealthFlag = false;

    private List<BluetoothGattService> services = null;
    private List<BluetoothGattCharacteristic> characteristics = null;

    ReadingClass charReading = new ReadingClass();

    private final IBinder mBinder = new LocalBinder();

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DESCRIPTOR_WRITE =
            "Descriptor update";
    public final static String ACTION_TEMPERATURE_UPDATE =
            "com.example.bluetooth.le.ACTION_TEMPERATURE_UPDATE";
    public final static String ACTION_SPO2_UPDATE =
            "com.example.bluetooth.le.ACTION_SPO2_UPDATE";
    public final static String ACTION_BPM_UPDATE =
            "com.example.bluetooth.le.ACTION_BPM_UPDATE";
    public final static String FINISHED_EHEALTH_DISCOVERY =
            "com.example.bluetooth.le.FINISHED_EHEALTH_DISCOVERY";
    public final static String CHARACTERISTIC_WRITE =
            "com.example.bluetooth.le.CHARACTERISTIC_WRITE";
    public final static String CHARACTERISTIC_READ =
            "com.example.bluetooth.le.CHARACTERISTIC_READ";
    public final static String ACTION_ECG_UPDATE =
            "com.example.bluetooth.le.ACTION_ECG_UPDATE";
    public final static String ACTION_ECG_ALPHA_UPDATE =
            "com.example.bluetooth.le.ACTION_ECG_ALPHA_UPDATE";
    public final static String NO_EHEALTH_SERVICE =
            "com.example.bluetooth.le.NO_EHEALTH_SERVICE";

    public final static String PULSE_OXYGEN_SATURATION = "PulseOxymeterSaturation";
    public final static String PULSE_BPM = "PulseOxymeterBPM";
    public final static String ECG = "ECG";
    public final static String EMG = "EMG";
    public final static String TEMPERATURE = "Temperature";
    public final static String AIRFLOW = "Airflow";
    public final static String BLOODPRESSURE = "BloodPressure";
    public final static String POSITION = "Position";
    public final static String GLUCOMETER = "Glucometer";
    public final static String GSR = "GSR";

    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //Create notification on service start
    @Override
    public void onCreate() {
        super.onCreate();

        Intent notificationIntent = new Intent(this, BluetoothLeService.class);
        context = this.getApplicationContext();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("eHealth Monitor")
                .setContentText("Background service running")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
        db = new DatabaseHelper(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        db.openDatabase();
    }

    //Remove notification on service destroy
    @Override
    public void onDestroy(){
        super.onDestroy();
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) context.getSystemService(ns);
        nMgr.cancel(1337);
        db.closeDatabase();
    }

    //Register broadcast receiver
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        registerReceiver(mBluetoothReceiver, makeBluetoothIntentFilter());
        return START_STICKY;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case "TemperatureProcessing":
                BluetoothGattCharacteristic characteristic = setCharacteristic(GattAttributes.TEMPERATURESERVICE, GattAttributes.TEMPERATUREPROCESSING);
                WriteCharacteristics write = new WriteCharacteristics(characteristic,sharedPreferences.getInt("TemperatureProcessing",1));
        }
    }

    //Set up connection when activity binds

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        running = true;
        return mBinder;
    }

    //Remove bound connection
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        running = false;
        //close();
        return super.onUnbind(intent);
    }

    //public function to add notifiable characteristic to queue
    public void addNotifiableCharacteristic(BluetoothGattCharacteristic characteristic) {
        Log.i("addNotifiable", "here");
        notifiableCharacteristics.add(characteristic);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        //Callback when connection state changes
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (DEBUG)
                Log.i("StateChange", "Enter");

            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                broadcastUpdate(AlarmReceiver.CHECK_ALARM_CANCELED);

                intentAction = ACTION_GATT_CONNECTED;

                //Set the connection state of the relavent board
                if(gatt.getDevice().getAddress().equals(mainBoardAddress)){
                    mConnectionState = STATE_CONNECTED;
                }

                else if(gatt.getDevice().getAddress().equals(subBoard1Address)){
                    mConnectionStateSubBoard = STATE_CONNECTED;
                }

                if (DEBUG)
                    Log.i(TAG, "Connected to GATT server.");

                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        gatt.discoverServices());

                //Send broadcast with connection state
                broadcastUpdate(intentAction);
            }

            //if connection disconnects close to clear adapter properly
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                intentAction = ACTION_GATT_DISCONNECTED;

                //Set connection state for relavent board and close connection
                if(gatt.getDevice().getAddress().equals(mainBoardAddress)){
                    mConnectionState = STATE_DISCONNECTED;
                    close(gatt);
                    mBluetoothGatt = null;
                    if(mBluetoothGatt2 != null)
                        mBluetoothGatt2.disconnect();

                }
                else if(gatt.getDevice().getAddress().equals(subBoard1Address)){
                    mConnectionStateSubBoard = STATE_DISCONNECTED;
                    close(gatt);
                    mBluetoothGatt2 = null;
                }

                if (DEBUG)
                    Log.i(TAG, "Disconnected from GATT server.");

                //clear the writeCharacteristics queue if disconnected
                writeQueue.clear();


                //start alarmreceiver if autoconnect enabled
                if(BluetoothPreferences.getDefaultAutoConnect(getApplicationContext())){
                    startAlarm();
                }

                //update activities to status of connection
                broadcastUpdate(intentAction);
            }
        }

        //callback when services have been discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("Gatt", gatt.getDevice().toString());
                BluetoothGattCharacteristic characteristic;
                //charLocation = 0;

                services = getSupportedGattServices(gatt);

                if (services != null) {
                    readQueue.clear();
                    notifiableCharacteristics.clear();
                    int returnValue;
                    //Returns -1 if read characterstics or -2 notify characteristics
                    //serviceLocation = getCharacteristics(services, 0, 0);
                    returnValue = getCharacteristics(services, 0, 0);

                    //if main board
                    if(gatt.getDevice().getName().equals("Paediatric Monitor")) {
                        if (!ehealthFlag) {
                            broadcastUpdate(NO_EHEALTH_SERVICE);
                            return;
                        }
                    }

                    //if not notifiable characteristics or read characteristics

                    if (returnValue != -2) {
                        readCharacteristic(readQueue.poll());

                    }
                    //if notifiable or read characteristics start polling notifiable
                    else {
                        characteristic = notifiableCharacteristics.poll();
                        if(characteristic != null){
                            setNotifiable(characteristic,gatt,true);
                        }

                    }

                    //Broadcast services discovered
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                }
            }

        }
        //Called when a characteristic has a value written to it
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (DEBUG)
                Log.i("CharacteristicWrite", "In callback");
            super.onCharacteristicWrite(gatt, characteristic, status);

            //Clear write flag
            writeInProgress = false;

            //If write successful, check write queue and write next characteristic if required
            if (status == BluetoothGatt.GATT_SUCCESS) {
                WriteCharacteristics w = writeQueue.poll();
                if (w != null) {
                    writeCharacteristic(w);
                }
            }

        }

        //Called when a characteristic is read
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (DEBUG)
                Log.i("CharacteristicRead", "here");
            //if read succesfully
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //clear the read flag
                readFlag = false;
                if (DEBUG)
                    Log.i("CharacteristicRead", "sending");

                //breadcast the read data
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

                //see if more characteristics are to be read
                BluetoothGattCharacteristic nextRequest = readQueue.poll();
                if (nextRequest != null) {
                    gatt.readCharacteristic(nextRequest);

                }

                //if finished reading
                else {

                    readFlag = false;
                    //if enumerating eHealth board
                    if (ehealthFlag) {

                        ehealthFlag = false;
                        //if finished broadcast finish the board
                        if(!serviceReconnecting)
                            broadcastUpdate(FINISHED_EHEALTH_DISCOVERY);
                        else
                            continueDiscovery();
                        //serviceLocation++;
                    }
                    //if enumerating a sensor
                    else if (sensorFlag) {
                        sensorFlag = false;
                        setNotifiableCharacteristics(gatt);
                    }
                }



            } else
                Log.i("Read", "fail");
        }

        //Called when a descriptor is written
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            //find if more notifiable characteristics
            BluetoothGattCharacteristic characteristic = notifiableCharacteristics.poll();

            if (characteristic != null) {
                setNotifiable(characteristic,gatt);
            }
            else {
                if (!serviceReconnecting)
                    broadcastUpdate(ACTION_DESCRIPTOR_WRITE, gatt.getDevice().getName());
                //if finished setting notifiable characteristics on main board, connect to secondary board
                else{
                    if(gatt.getDevice().getName().equals("Paediatric Monitor")){
                        String address = sharedPreferences.getString("additionalBoard1Address",null);
                        if(!address.equals(null))
                            connect2(address);
                    }
                    else
                        serviceReconnecting = false;
                }

            }
        }

        //Called when a characteristic changes it's value
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            //find the time
            charReading.setTime(System.currentTimeMillis() / 1000);
            super.onCharacteristicChanged(gatt, characteristic);

            int intValue;
            byte characteristicByteValue[];
            if(DEBUG) {
                Log.i("Char", characteristic.getUuid().toString());
            }
            //find out which characteristic has changed
            switch (characteristic.getUuid().toString()) {

                //set the value for SPO2 reading
                case GattAttributes.SPO2VALUECHAR:
                    characteristicByteValue = characteristic.getValue();

                    //swap bytes
                    intValue = (characteristicByteValue[0] << 8) | (characteristicByteValue[1] & 0xff);

                    Log.i("SPO2Value", Integer.toString(intValue));
                    broadcastUpdate(ACTION_SPO2_UPDATE, intValue);
                    charReading.setType(PULSE_OXYGEN_SATURATION);
                    charReading.setValueInt(intValue);
                    break;

                //set the value for heart rate
                case GattAttributes.BPMVALUECHAR:

                    characteristicByteValue = characteristic.getValue();

                    //swap bytes
                    intValue = (characteristicByteValue[0] << 8) | (characteristicByteValue[1] & 0xff);
                    Log.i("BPMValue", Integer.toString(intValue));
                    broadcastUpdate(ACTION_BPM_UPDATE, intValue);
                    charReading.setType(PULSE_BPM);
                    charReading.setValueInt(intValue);
                    break;

                //set the value for temperature reading
                case GattAttributes.TEMPERATUREVALUECHAR:
                    characteristicByteValue = characteristic.getValue();
                    //check checksum
                    if((characteristicByteValue[0] ^ characteristicByteValue[1]) == characteristicByteValue[2]) {
                        //swap bytes
                        intValue = (characteristicByteValue[0] << 8) | (characteristicByteValue[1] & 0xff);

                        Log.i("Temperature Value", Integer.toString(intValue));
                        broadcastUpdate(ACTION_TEMPERATURE_UPDATE, intValue);

                        charReading.setType(TEMPERATURE);
                        charReading.setValueInt(intValue);
                    }
                    else{
                        return;
                    }

                    break;

                //set value for ecg
                case GattAttributes.ECGVALUECHAR:
                    characteristicByteValue = characteristic.getValue();
                    //swap bytes
                    intValue = (characteristicByteValue[0] << 8) | (characteristicByteValue[1] &0xff);
                    broadcastUpdate(ACTION_ECG_UPDATE,intValue);
                    charReading.setType(ECG);
                    charReading.setValueInt(intValue);
                    break;

                //set value for ecg using SAX
                case GattAttributes.ECGALPHAVALUECHAR:
                    String alphaString = characteristic.getStringValue(0);
                    Log.i("StringValue",alphaString);
                    broadcastUpdate(ACTION_ECG_ALPHA_UPDATE,alphaString);

                default:
                    return;

            }

            //add the reading to the database
            db.addReading(charReading);
        }
    };


    //set of four functions to broadcast update depending on data type
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final int value) {
        final Intent intent = new Intent(action);
        intent.putExtra("value", value);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String value) {
        final Intent intent = new Intent(action);
        intent.putExtra("value", value);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        intent.putExtra("parentService", characteristic.getService().getUuid().toString());

        intent.putExtra("characteristic", characteristic.getUuid().toString());
        int convertInt;

        //Change data according to transmitted sensor type
        if(characteristic.getUuid().toString().equals(GattAttributes.TEMPERATURESAMPLECHAR)){
            convertInt = reverseInt(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0));
            intent.putExtra("value",convertInt);
            Log.i("WConvertInt", String.valueOf(convertInt));
        }
        else if(characteristic.getUuid().toString().equals(GattAttributes.EXTRABOARD1)){
            intent.putExtra("value",characteristic.getStringValue(0));
        }

        else {
            convertInt = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

            intent.putExtra("value", convertInt);
            if (convertInt > 0)
                intent.putExtra("valueBoolean", true);
            else
                intent.putExtra("valueBoolean", false);
        }
        Log.i("Broadcast", "sent");
        sendBroadcast(intent);
    }

     /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close(BluetoothGatt gatt) {
        if (gatt != null) {
            gatt.close();
        }
    }

    //Connect to BLE device
    //Each connection requires a dedicated bluetooth Gatt
    public boolean connect(final String address) {
        serviceLocation = 0;
        Log.i("Address",address);
        mainBoardAddress = address;
        if (mBluetoothAdapter == null || address == null) {
            Log.i(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.i(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.i(TAG, "Trying to create a new connection.");
        refreshDeviceCache(mBluetoothGatt);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    //Connect to second device
    public boolean connect2(String address) {
        subBoard1Address = address;
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.i(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt2 = device.connectGatt(this, false, mGattCallback);
        Log.i(TAG, "Trying to create a new connection.");
        refreshDeviceCache(mBluetoothGatt2);
        return true;
    }

    //Continue to discover available services and characteristics
    public void continueDiscovery() {
        serviceLocation++;
        Log.i("Service Location",String.valueOf(serviceLocation));
        readQueue.clear();
        notifiableCharacteristics.clear();
        getCharacteristics(services, serviceLocation, 0);
    }

    //Disconnect from BLE device
    public void disconnect() {
        if (mBluetoothAdapter != null || mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    //Discover characteristics from BLE device
    private int getCharacteristics(List<BluetoothGattService> services, int servicePosition, int characteristicPosition) {

        int currentCharPosition = characteristicPosition;

        BluetoothGattService service;

        //for each discovered service
        for (int i = servicePosition; i < services.size(); i++) {
            service = services.get(i);
            if(DEBUG) {
                Log.i("CurrentPos", Integer.toString(servicePosition));
                Log.i("CurrentChar", Integer.toString(currentCharPosition));
                Log.i("UUID", service.getUuid().toString());
            }

            //is it one of the required services
            switch (service.getUuid().toString()) {
                case GattAttributes.EHEALTHSERVICE:
                    characteristics = service.getCharacteristics();

                    //only read characteristics exist for the eHealth service so add them to the read Queue
                    for (BluetoothGattCharacteristic tempCharacteristic : characteristics) {
                        readQueue.add(tempCharacteristic);
                    }

                    ehealthFlag = true;
                    return i;

                //if it's one of the services
                case GattAttributes.PULSIOXIMETERSERVICE:

                case GattAttributes.TEMPERATURESERVICE:

                case GattAttributes.ECGSERVICE:

                    characteristics = service.getCharacteristics();

                    //both read and notify characteristics exist so separate
                    for (BluetoothGattCharacteristic tempCharacteristic : characteristics) {
                        if ((tempCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                            readQueue.add(tempCharacteristic);
                        } else if ((tempCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                            addNotifiableCharacteristic(tempCharacteristic);
                        }
                    }
                    break;
                default:
                    break;
            }
            currentCharPosition = 0;
            serviceLocation++;

        }
        sensorFlag = true;

        //if read characteristics exist then send request for the first one
        if (!readQueue.isEmpty()) {
            readCharacteristic(readQueue.poll());
        }
        else if(!notifiableCharacteristics.isEmpty()){
            return -2;
        }

        return -1;
    }

    //return main connection state
    public int getConnectionState() {
        return mConnectionState;
    }

    //find if discovery finished
    public boolean getFinishedDiscovery(){
        return this.finishedDiscovery;
    }

    //Get the supported services
    public List<BluetoothGattService> getSupportedGattServices(BluetoothGatt gatt) {
        if (gatt == null) return null;

        return gatt.getServices();
    }


    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    //define broadcast to list for
    private static IntentFilter makeBluetoothIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CHARACTERISTIC_WRITE);
        intentFilter.addAction(CHARACTERISTIC_READ);
        intentFilter.addAction(AlarmReceiver.ACTION_DEVICE_REFOUND);
        intentFilter.addAction(AlarmReceiver.CHECK_ALARM_CANCELED);

        return intentFilter;
    }

    //Broadcast reciever
    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            //change characteristic
            //type = 0 for 16bit number
            //type = 1 for 8 bit number
            //type = 2 for 32 bit number
            if (CHARACTERISTIC_WRITE.equals(action)) {
                BluetoothGattCharacteristic characteristic = setCharacteristic(intent.getStringExtra("parentService"),intent.getStringExtra("characteristic"));
                int type = intent.getIntExtra("type",-1);
                WriteCharacteristics mWriteCharacteristic ;
                //send 16 bit number
                if(type == 0){
                    int value = intent.getIntExtra("value",1);

                    //reverse the value
                    int reverseValue = reverseInt16(value);

                    mWriteCharacteristic = new WriteCharacteristics(characteristic,reverseValue);
                    mWriteCharacteristic.setType(2);
                    if(characteristic!=null){
                        writeCharacteristic(mWriteCharacteristic);
                    }

                }

                //send 8 bit number
                else if(type == 1){
                    int value = intent.getIntExtra("value",1);
                    mWriteCharacteristic = new WriteCharacteristics(characteristic,value);
                    mWriteCharacteristic.setType(0);

                    if(characteristic!=null){
                        writeCharacteristic(mWriteCharacteristic);
                    }

                }

                //Send 32bit number
                else if(type == 4) {
                    int value = intent.getIntExtra("value", 1);
                    Log.i("Characteristic value", String.valueOf(value));
                    int reverseValue = reverseInt(value);
                    mWriteCharacteristic = new WriteCharacteristics(characteristic, reverseValue);

                    mWriteCharacteristic.setType(4);
                    if (characteristic != null) {
                        writeCharacteristic(mWriteCharacteristic);
                        Log.i("Sending characteristic","Now");

                    }
                }
            }

            else if(CHARACTERISTIC_READ.equals(action)) {

                BluetoothGattCharacteristic characteristic = setCharacteristic(intent.getStringExtra("parentService"),intent.getStringExtra("characteristic"));
                readCharacteristic(characteristic);

            }
            else if (AlarmReceiver.ACTION_DEVICE_REFOUND.equals(action)){
                Log.i("Main","Refound");
                stopAlarm();
                if(BluetoothPreferences.getDefaultAutoConnect(getApplicationContext())) {
                    String deviceAddress = sharedPreferences.getString(MainActivity.DEVICE_ADDRESS, MainActivity.NO_DEVICE);
                    if (!deviceAddress.equals(MainActivity.NO_DEVICE)) {
                        serviceReconnecting = true;
                        connect(deviceAddress);
                    }
                }
            }
            else if (AlarmReceiver.CHECK_ALARM_CANCELED.equals(action)){
                stopAlarm();
            }
        }
    };


    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if(!readFlag) {
            try{
                readFlag = true;
                mBluetoothGatt.readCharacteristic(characteristic);
            }
            catch(NullPointerException ex){
            }
        }
        else
        if(characteristic != null)
            readQueue.add(characteristic);
    }


    // There is a refresh() method in BluetoothGatt class but for now it's hidden.
    // It can be called using reflections.


    public static boolean refreshDeviceCache(BluetoothGatt gatt) {

        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                final boolean success = (Boolean) refresh.invoke(gatt);
                Log.i(TAG, "Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured while refreshing device", e);
        }
        return false;
    }

    //function to reverse bytes for 16 bit number
    private int reverseInt16(int i){
        return (i&0xff00) >> 8 | (i&0x00ff)<< 8;
    }

    //function to reverse bytes for 32 bit number
    private int reverseInt(int i) {
        return (i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff;
    }

    //Create a characteristic from two strings representing the service and the characteristic
    public BluetoothGattCharacteristic setCharacteristic(String serviceString, String characteristicString) {
        BluetoothGattCharacteristic characteristic;
        BluetoothGattService service;
        UUID serviceUUID, characteristicUUID;
        serviceUUID = UUID.fromString(serviceString);
        characteristicUUID = UUID.fromString(characteristicString);

        try {
            service = mBluetoothGatt.getService(serviceUUID);
            if (service != null)
                characteristic = service.getCharacteristic(characteristicUUID);
            else
                characteristic = null;
        } catch (Exception ex) {
            characteristic = null;
        }
        return characteristic;
    }

    public void setFinishedDiscovery(boolean state){
        finishedDiscovery=state;
    }

    /*public void setNotifiable(BluetoothGattCharacteristic characteristic) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }*/

    public void setNotifiable(BluetoothGattCharacteristic characteristic,BluetoothGatt gatt, boolean enable) {
        gatt.setCharacteristicNotification(characteristic, enable);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    public void setNotifiable(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        if(!finishedSetNotify) {
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

    public void setNotifiableCharacteristics(BluetoothGatt gatt) {

        BluetoothGattCharacteristic characteristic = notifiableCharacteristics.poll();
        if (characteristic != null) {
            Log.i("SetNote", "here");

            setNotifiable(characteristic, gatt, true);

        }
        finishedDiscovery = true;

    }

    public boolean startAlarm(){
        startAlarmReceiver();
        return true;
    }

    private void startAlarmReceiver(){
        Intent IntentAlarm = new Intent(this,AlarmReceiver.class);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        long delay = interval * 60000;

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 30, delay, PendingIntent.getBroadcast(this, 111, IntentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

    }

    public boolean stopAlarm(){
        stopAlarmReceiver();
        return true;
    }

    private void stopAlarmReceiver(){
        Intent IntentAlarm = new Intent(this, AlarmReceiver.class);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        stopService(IntentAlarm);

        alarmManager.cancel(PendingIntent.getBroadcast(this, 111, IntentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    //Send characteristic
    //Write type = 0 , 8 bit number
    //Write type = 1 , String
    //Write type = 2 , 16 bit number
    //Write type = 3 , 32 bit number
    public void writeCharacteristic(WriteCharacteristics write) {
        Log.i("in","Write char");
        if (!writeInProgress && (mConnectionState == STATE_CONNECTED)) {
            Log.i("in","if char");
            writeInProgress = true;
            BluetoothGattCharacteristic characteristic = write.getCharacteristic();

            if (write.getType() == 0) {
                characteristic.setValue(write.getIntValue(), BluetoothGattCharacteristic.FORMAT_UINT8, 0);

            } else if (write.getType() == 1) {
                characteristic.setValue(write.getStringValue());

            }
            else if(write.getType() == 2 ){
                characteristic.setValue(write.getIntValue(),BluetoothGattCharacteristic.FORMAT_UINT16,0);
            }
            else if(write.getType() == 4) {
                characteristic.setValue(write.getIntValue(), BluetoothGattCharacteristic.FORMAT_UINT32, 0);
            }

            mBluetoothGatt.writeCharacteristic(characteristic);
        } else {
            Log.i("in","else char");
            if(mConnectionState == STATE_CONNECTED) {
                writeQueue.add(write);
            }
        }
    }

}

