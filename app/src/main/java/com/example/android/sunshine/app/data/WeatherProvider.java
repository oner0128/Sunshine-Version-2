package com.example.android.sunshine.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by hrong on 2016/9/23.
 */

public class WeatherProvider extends ContentProvider {

    private static final UriMatcher mUriMatcher = buildUriMatcher();

    private WeatherDbHelper wOpenHelper;
    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;
    private static final SQLiteQueryBuilder WEATHER_BY_LOCATION_SETTING_QUERY_BUILDER;

    static {
        WEATHER_BY_LOCATION_SETTING_QUERY_BUILDER = new SQLiteQueryBuilder();
        WEATHER_BY_LOCATION_SETTING_QUERY_BUILDER.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
    }

    private static final String sLocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME + "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
    private static final String sLcationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME + "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
                    + " = ? AND " + WeatherContract.WeatherEntry.COLUMN_DATE + " >= ? ";
    private static final String sLocationSettingAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME + "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
                    + " = ? AND " + WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);
        String[] selectionArgs;
        String selection;
        if (startDate == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selection = sLcationSettingWithStartDateSelection;
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
        }
        return WEATHER_BY_LOCATION_SETTING_QUERY_BUILDER.query(
                wOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long Date = WeatherContract.WeatherEntry.getDateFromUri(uri);

        return WEATHER_BY_LOCATION_SETTING_QUERY_BUILDER.query(
                wOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(Date)},
                null, null,
                sortOrder);
    }


    static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);
        uriMatcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        wOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor;
        switch (mUriMatcher.match(uri)) {
            case WEATHER_WITH_LOCATION: {
                returnCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            case WEATHER_WITH_LOCATION_AND_DATE: {
                returnCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            case WEATHER: {
                returnCursor = wOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case LOCATION: {
                returnCursor = wOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("unKnown uri:" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = wOpenHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case WEATHER: {
//                normalizeDate(contentValues);
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = WeatherContract.WeatherEntry.builtWeatherUri(_id);
                else throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = WeatherContract.LocationEntry.builtWeatherUri(_id);
                else throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("unKnown uri :" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

//    private void normalizeDate(ContentValues values) {
//        // normalize the date value
//        if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)) {
//            long dateValue = values.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
//            values.put(WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.normalizeDate(dateValue));
//        }
//    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = wOpenHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        int isDelete;
        if (selection == null) selection = "1";
        switch (match) {
            case WEATHER: {
                isDelete = db.delete(WeatherContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case LOCATION: {
                isDelete = db.delete(WeatherContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("unKnown uri " + uri);
        }
        if (isDelete != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return isDelete;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = wOpenHelper.getWritableDatabase();
        final int isUpdate;
        switch (mUriMatcher.match(uri)) {
            case WEATHER: {
//                normalizeDate(contentValues);
                isUpdate = db.update(WeatherContract.WeatherEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            }
            case LOCATION: {
                isUpdate = db.update(WeatherContract.LocationEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("unKnown uri " + uri);
        }
        if (isUpdate != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return isUpdate;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = wOpenHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case WEATHER: {
                db.beginTransaction();
                int returnInt = 0;
                try {
                    for (ContentValues value : values) {
//                        normalizeDate(value);
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnInt++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnInt;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
