package com.example.android.sunshine.app.service;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SunShineService extends IntentService {
    public static final String LOG_TAG = SunShineService.class.getName();
    public static final String LOCATION_QUERY_EXTRA = "lqe";
    public static final String UNIT_TYPE = "unitType";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.android.sunshine.app.service.action.FOO";
    private static final String ACTION_BAZ = "com.example.android.sunshine.app.service.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.android.sunshine.app.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.android.sunshine.app.service.extra.PARAM2";

    public SunShineService() {
        super("SunShineService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SunShineService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SunShineService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
            String locationQuery = intent.getStringExtra(SunShineService.LOCATION_QUERY_EXTRA);
            String unitType = intent.getStringExtra(SunShineService.UNIT_TYPE);

            HttpURLConnection httpURLConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

//            String format = "xml";
            String locationSetting = locationQuery;
            String unitsType = unitType;
            String type = "accurate";
            String appid = "b21e787cebb54337b23e4816da79da62";

            try {
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
                final String POSTCODE_PARAM = "id";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String TYPE_PARAM = "type";
                final String APPID_PARAM = "APPID";

                Uri.Builder buildUri = Uri.parse(FORECAST_BASE_URL).buildUpon().
                        appendQueryParameter(POSTCODE_PARAM, locationSetting).
                        appendQueryParameter(UNITS_PARAM, unitsType).
                        appendQueryParameter(TYPE_PARAM, type).
                        appendQueryParameter(APPID_PARAM, appid);
                Log.v(LOG_TAG, "Built Uri :" + buildUri);

                URL url = new URL(buildUri.toString());

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (inputStream == null) {
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuffer.append(line + "\n");
                }
                if (stringBuffer.length() == 0) {
                    return;
                }
                forecastJsonStr = stringBuffer.toString();
                Log.v(LOG_TAG, "forecast JSON string :" + forecastJsonStr);
                getWeatherDataFromJson(forecastJsonStr, locationSetting);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(LOG_TAG, "ERROR", e);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "ERROR Closing reader", e);
                    }
                }
            }
        }
    }


    private void getWeatherDataFromJson(String weatherJsonStr, String locationSetting)
            throws JSONException {
        final String JSON_LIST = "list";
        final String JSON_date = "dt";
        final String JSON_MAIN = "main";
        final String JSON_CITY = "city";
        final String JSON_CITYNAME = "name";
        final String JSON_WEATHER = "weather";
        final String JSON_DESCRIPTION = "description";
        final String JSON_TEMP_MAX = "temp_max";
        final String JSON_TEMP_MIN = "temp_min";
        final String JSON_COORD = "coord";
        final String JSON_LAT = "lat";
        final String JSON_LON = "lon";
        final String JSON_WEATHER_ID = "id";
        final String JSON_HUMIDITY = "humidity";
        final String JSON_PRESSURE = "pressure";
        final String JSON_WIND = "wind";
        final String JSON_WIND_SPEED = "speed";
        final String JSON_WIND_DEGREES = "deg";

        JSONObject weatherObject = new JSONObject(weatherJsonStr);
        JSONObject city = weatherObject.getJSONObject(JSON_CITY);
        String cityName = city.getString(JSON_CITYNAME);
        JSONObject coord = city.getJSONObject(JSON_COORD);
        double lat = coord.getDouble(JSON_LAT);
        double lon = coord.getDouble(JSON_LON);

        JSONArray days = weatherObject.getJSONArray(JSON_LIST);

        long locationId = addLocation(locationSetting, cityName, lat, lon);

        Vector<ContentValues> valuesVector = new Vector<>(days.length());

        for (int i = 0; i < days.length(); i++) {
            double windSpeed;
            double degrees;
            double humidity;
            double pressure;
            double temp_max;
            double temp_min;
            String description;
            int weather_id;
            long dateTime;

            JSONObject dayInfo = days.getJSONObject(i);
            dateTime = dayInfo.getLong(JSON_date);
            description = dayInfo.getJSONArray(JSON_WEATHER).getJSONObject(0).getString(JSON_DESCRIPTION);
            weather_id = dayInfo.getJSONArray(JSON_WEATHER).getJSONObject(0).getInt(JSON_WEATHER_ID);
            JSONObject windInfo = dayInfo.getJSONObject(JSON_WIND);
            windSpeed = windInfo.getDouble(JSON_WIND_SPEED);
            degrees = windInfo.getDouble(JSON_WIND_DEGREES);

            JSONObject mainInfo = dayInfo.getJSONObject(JSON_MAIN);
            humidity = mainInfo.getDouble(JSON_HUMIDITY);
            pressure = mainInfo.getDouble(JSON_PRESSURE);
            temp_max = mainInfo.getDouble(JSON_TEMP_MAX);
            temp_min = mainInfo.getDouble(JSON_TEMP_MIN);

            ContentValues weatherValues = new ContentValues();


            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weather_id);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, temp_max);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, temp_min);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, degrees);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
            valuesVector.add(weatherValues);
        }
        if (valuesVector.size() > 0) {
            ContentValues[] contentValues = new ContentValues[valuesVector.size()];
            valuesVector.toArray(contentValues);
            this.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, contentValues);
        }

        Log.d(LOG_TAG, "FetchWeatherTask complete." + valuesVector.size() + " inserted");
    }

    public long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;

        Cursor locationCursor = this.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);
        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            ContentValues locationValues = new ContentValues();

            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LON, lon);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);

            Uri insertedUri = this.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI, locationValues);
            locationId = ContentUris.parseId(insertedUri);
        }
        locationCursor.close();
        return locationId;
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
