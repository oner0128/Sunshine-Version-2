package com.example.android.sunshine.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by hrong on 2016/6/1.
 */
public class WeatherDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "weather.db";
    public static final String TAG_LOG =WeatherDbHelper.class.getSimpleName();

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + WeatherContract.LocationEntry.TABLE_NAME + " ("
                + WeatherContract.LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, "
                + WeatherContract.LocationEntry.COLUMN_CITY_NAME + " INTEGER NOT NULL, "
                + WeatherContract.LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, "
                + WeatherContract.LocationEntry.COLUMN_COORD_LON + " REAL NOT NULL" + " );";

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherContract.WeatherEntry.TABLE_NAME + " ("
                + WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL,"
                + WeatherContract.WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL,"
                + WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL,"
                + WeatherContract.WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL,"

                + WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL,"
                + WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL,"
                + WeatherContract.WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL,"
                + WeatherContract.WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL,"
                + WeatherContract.WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL,"
                + WeatherContract.WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, "

                // Set up the location column as a foreign key to location table.
                + "FOREIGN KEY (" + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES "
                + WeatherContract.LocationEntry.TABLE_NAME + " (" + WeatherContract.LocationEntry._ID + "), " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + WeatherContract.WeatherEntry.COLUMN_DATE + ", "
                + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
        Log.d(TAG_LOG,"SQL_CREATE_LOCATION:"+SQL_CREATE_LOCATION_TABLE);
        Log.d(TAG_LOG,"SQL_CREATE_WEATHER:"+SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ WeatherContract.WeatherEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ WeatherContract.LocationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
