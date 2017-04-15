package com.example.matija077.autotrolej;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.matija077.autotrolej.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by Matija077 on 4/14/2017.
 */

public class databaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "autotrolej.db";
    public static List<String> databaseTables = new ArrayList<String>();

    //if we want only context
    public databaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    //normal constructor
    public databaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION, errorHandler);
    }

    //adding tableList for later implementing somethign else except hardcoding.
    public databaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler, List<String> tableList) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION, errorHandler);

        databaseTables = tableList;
        /*for (int i = 0; i <  databaseTables.size(); i++) {

        }*/
    }

    //Table names
    public static final String TABLE_STATION = " station";

    //Common colum names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    //Common database statements
    private static final String DROP_IF_EXISTS = "DROP TABLE IF EXISTS ";
    private static final String SELECT = "SELECT * FROM ";
    private static final String DROP = "DROP TABLE ";

    //Staion Table - column names
    private static final String NAME = "name";
    private static final String GPSX = "gpsx";
    private static final String GPSY = "gpsy";
    private static final String ZONE = "zone";

    //db.query in getStation method accepts object [] as its column parametar.
    /*private static  final List<String> STATION_COLUMN_LIST = new ArrayList<String>() {{
        add(NAME);
        add(GPSX);
        add(GPSY);
        add(ZONE);
    }};*/

    //Station create statement
    private static final String CREATE_TABLE_STATION = "CREATE TABLE"
            + TABLE_STATION + "(" + KEY_ID + " INTEGER PRIMARY KEY," + NAME + " TEXT,"
            + GPSX +  " FLOAT," + GPSY + " FLOAT," + ZONE + " SHORT" + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_IF_EXISTS + TABLE_STATION);

        onCreate(db);

    }

    //close connection
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    //CRUD

    // For now everything is hardcoded :(
    //TODO: dont hard code it

    //Create

    //Create table Station
    public long createStation(Station station) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (Exception e) {
            Log.d(TAG, "createStation", e);
        }
        ContentValues values = new ContentValues();
        values.put(NAME, station.getName());
        values.put(GPSX, station.getGpsx());
        values.put(GPSY, station.getGpsy());
        values.put(ZONE, station.getZone());

        long station_id = db.insert(TABLE_STATION, null, values);

        return station_id;
    }

    //Get
    //Get  Station
    public Station getStation(long station_id) {
        String [] station_id_list = new String[] {String.valueOf(station_id)};
        SQLiteDatabase db = this.getReadableDatabase();
        String[] STATION_COLUMN_LIST = {
                NAME, GPSX, GPSY, ZONE
        };


        Cursor c = null;
        try {
            c = db.query(TABLE_STATION, STATION_COLUMN_LIST, KEY_ID, station_id_list, null, null, null, null);
        } catch (Exception e) {
            Log.d(TAG, "getStation()", e);
        }

        Station station = new Station();
        /*station.setName(c.getString(c.getColumnIndex(NAME)));
        station.setGpsx(c.getDouble(c.getColumnIndex(GPSX)));
        station.setGpsy(c.getDouble(c.getColumnIndex(GPSY)));
        station.setZone(c.getShort(c.getColumnIndex(ZONE)));
        */
        return station;
    }

    //get table Station
    public List<Station> getAllStations() {
        List<Station> stations = new ArrayList<Station>();
        String select = SELECT + TABLE_STATION;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(select, null);

        if (c.moveToFirst()) {
            do {
                Station station = new Station();
                station.setName(c.getString(c.getColumnIndex(NAME)));
                station.setGpsx(c.getDouble(c.getColumnIndex(GPSX)));
                station.setGpsy(c.getDouble(c.getColumnIndex(GPSY)));
                station.setZone(c.getShort(c.getColumnIndex(ZONE)));

                stations.add(station);
            } while (c.moveToNext());
        }

        return stations;
    }

    //Delete

    //Drop table
    public Boolean dropTable(String table_name) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.execSQL(DROP_IF_EXISTS + table_name);
        } catch(Exception e) {
            Log.d(TAG, "dropTable()", e);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

}
