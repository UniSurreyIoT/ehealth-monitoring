package com.example.phil.bluetoothgatt;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Phil on 16/01/2016.
 * Helper class to provide access to database
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private boolean debug = false;


    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "MonitoredReadings";

    // Contacts table name
    private static final String TABLE_READINGS = "Test";

    // Contacts Table Columns names
    private static final String KEY_TIME = "Time";
    private static final String KEY_VALUE_INT = "ValueInt";
    private static final String KEY_VALUE_STRING = "ValueString";
    private static final String KEY_TYPE = "Type";

    private static final String[] COLUMNS = {KEY_TIME,KEY_VALUE_INT,KEY_VALUE_STRING,KEY_TYPE};

    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if(debug)
            Log.i("Created","Here");


    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_READINGS + "("
                + KEY_TIME + " INTEGER," + KEY_VALUE_INT + " INTEGER," + KEY_VALUE_STRING + " TEXT,"
                + KEY_TYPE + " TEXT" + ")";
        if(debug)
            Log.i("String",CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_READINGS);

        // Create tables again
        onCreate(db);
    }

    SQLiteDatabase database;
    void openDatabase(){
        database = this.getWritableDatabase();
    }

    void closeDatabase(){
        database.close();
    }
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    void addReading(ReadingClass reading) {
        //SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, reading.getTime()); // Contact Name
        if(reading.getValueInt() != -1){
            values.put(KEY_VALUE_INT, reading.getValueInt()); // Contact Phone
            values.put(KEY_VALUE_STRING,"");
        }
        else{
            values.put(KEY_VALUE_STRING,reading.getValueString());
            values.put(KEY_VALUE_INT,-1);
        }
        values.put(KEY_TYPE,reading.getType());

        // Inserting Row
        database.insert(TABLE_READINGS, null, values);
        //db.close();// Closing database connection

    }

    public void clearDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_READINGS,null,null);
    }


    public void getReadings(List<String> exportTypes, long startTime, long endTime,Context context){
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "(";
        String [] variables = new String[exportTypes.size() + 2];
        exportTypes.toArray(variables);
        for (int i = 0; i < (exportTypes.size() -1); i++){
            queryString += " Type = ? OR ";
        }
        queryString += "Type = ? )";

        queryString += " AND ";

        queryString += "( Time BETWEEN ? AND ? )";

        Log.i("QueryString",queryString);



        variables[exportTypes.size()] = String.valueOf(startTime);
        variables[exportTypes.size()+1] = String.valueOf(endTime);


        Cursor cursor =
                db.query(TABLE_READINGS,
                        COLUMNS,
                        queryString,
                        variables,
                        null,
                        null,
                        null,
                        null);

        if(cursor != null) {
            Log.i("Cursor", "not null");
            cursor.moveToFirst();
        }

        String[] entries = new String[4];
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try{
//            File directory = new File(Environment.getExternalStorageDirectory().getPath() + "/EHealth");
            File directory = new File("/sdcard/EHealth");

            if(!directory.exists()){
                directory.mkdir();
            }
            long timeMs = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("HHmmss_ddMMyyyy", Locale.ENGLISH);
            String currentDateandTime = sdf.format(timeMs);
            String fileName = "/sdcard/EHealth/" + currentDateandTime + ".csv";
            //String fileName = Environment.getExternalStorageDirectory().getPath() + "/EHealth/" + currentDateandTime + ".csv";


            editor.putString("currentFile",fileName);


            File file = new File(fileName);
            Log.i("File",file.getCanonicalPath());
            Log.i("Path",Environment.getRootDirectory().getAbsolutePath());
            if(!file.exists())
                file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);

            CSVWriter writer = new CSVWriter(fileWriter,',');
            Log.i("Writer","Open");
            do{
                //ReadingClass getReading = new ReadingClass();
                // feed in your array (or convert your data to an array)
                entries[0] = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
                entries[1] = sdf.format(cursor.getLong(cursor.getColumnIndex(KEY_TIME))*1000);
                entries[2] = Integer.toString(cursor.getInt(cursor.getColumnIndex(KEY_VALUE_INT)));
                entries[3] = cursor.getString(cursor.getColumnIndex(KEY_VALUE_STRING));
                if(debug) {
                    Log.i("Entry", entries[1]);
                }
                writer.writeNext(entries);


                cursor.moveToNext();
            }while(!cursor.isAfterLast());
            writer.close();
        }
        catch(Exception ex){
            editor.putString("currentFile",null);
            Log.i("Exception",ex.toString());
        }
        try{
            cursor.close();
        }
        catch(NullPointerException ex){

        }

        editor.apply();

        //return readings;
    }

    public List<ReadingClass> getReadings(String type) {
        SQLiteDatabase db = this.getReadableDatabase();

        List<ReadingClass> readings = new ArrayList<>();
        String readString;
        Cursor cursor =
                db.query(TABLE_READINGS, // a. table
                        COLUMNS, // b. column names
                        "Type = ? ", // c. selections
                        new String[] { type }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        if (cursor != null)
            cursor.moveToFirst();
        try {
            if (cursor.getInt(0) != 0) {


                do {

                    ReadingClass getReading = new ReadingClass();
                    readString = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
                    if (debug)
                        Log.i("REadstring", readString);
                    switch (readString) {
                        case BluetoothLeService.TEMPERATURE:
                            if (debug)
                                Log.i("Time in DatabaseHelper", Long.toString(cursor.getLong(cursor.getColumnIndex(KEY_TIME))));
                            getReading.setTime(cursor.getLong(cursor.getColumnIndex(KEY_TIME)));
                            getReading.setValueInt(cursor.getInt(cursor.getColumnIndex(KEY_VALUE_INT)));
                            getReading.setValueString(cursor.getString(cursor.getColumnIndex(KEY_VALUE_STRING)));
                            getReading.setType(readString);
                            break;
                        default:
                            break;
                    }
                    if (debug)
                        Log.i("AAA", Long.toString(getReading.getTime()));
                    readings.add(getReading);
                    if (debug)
                        Log.i("Readings size", Integer.toString(readings.size()));
                    cursor.moveToNext();
                } while (!cursor.isAfterLast());
                // return contact
            }
        }
        catch(CursorIndexOutOfBoundsException ex){
            //Put in a Toast database empty message;
        }
        cursor.close();
        for(ReadingClass r : readings){
            if(debug)
                Log.i("Rtime",Long.toString(r.getTime()));
        }
        return readings;
    }

    // Getting All Contacts
    public List<ReadingClass> getAllReadings() {

        List<ReadingClass> readingList = new ArrayList<ReadingClass>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_READINGS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ReadingClass reading = new ReadingClass();
                reading.setTime(cursor.getLong(1));
                reading.setValueInt(cursor.getInt(2));
                reading.setType(cursor.getString(2));
                // Adding contact to list
                readingList.add(reading);
            } while (cursor.moveToNext());
        }

        // return contact list
        return readingList;
    }

    // Getting contacts Count
    public int getReadingsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_READINGS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}

