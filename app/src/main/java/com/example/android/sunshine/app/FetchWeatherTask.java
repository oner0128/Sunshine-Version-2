package com.example.android.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
 * Created by hrong on 2016/9/26.
 */

public class FetchWeatherTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
//    private ArrayAdapter<String> forecastAdapter;
    private Context context;

    public FetchWeatherTask(Context context) {
        this.context = context;
//        this.forecastAdapter = forecastAdapter;
    }

    @Override
    protected Void doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;

//            String format = "xml";
        String locationSetting = params[0];
        String unitsType = params[1];
        String type = "accurate";
        String appid = "b21e787cebb54337b23e4816da79da62";

        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
            final String POSTCODE_PARAM = "id";
//                final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String TYPE_PARAM = "type";
            final String APPID_PARAM = "APPID";

            Uri.Builder buildUri = Uri.parse(FORECAST_BASE_URL).buildUpon().
                    appendQueryParameter(POSTCODE_PARAM, locationSetting).
//                        appendQueryParameter(FORMAT_PARAM, format).
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
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }
            if (stringBuffer.length() == 0) {
                return null;
            }
            forecastJsonStr = stringBuffer.toString();
            Log.v(LOG_TAG, "forecast JSON string :" + forecastJsonStr);
            getWeatherDataFromJson(forecastJsonStr,locationSetting);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "ERROR", e);
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
//                    return getWeatherDataFromJson(forecastJsonStr, num, locationSetting);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "ERROR Closing reader", e);
//                } catch (JSONException e) {
//                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void getWeatherDataFromJson(String weatherJsonStr, String locationSetting)
            throws JSONException {
        final String JSON_LIST = "list";
        final String JSON_date = "dt";
        final String JSON_MAIN = "main";
        final String JSON_TEMP = "temp";
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
//            String daystr = dateTime + " - " + description + " - " + formatMaxMintemp(temp_max, temp_min);
//            daysInfo[i] = cityName + " - " + daystr;

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
            context.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, contentValues);
        }
//        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC ";
//        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
//        Cursor cursor = context.getContentResolver().query(weatherForLocationUri, null, null, null, sortOrder);
//        valuesVector = new Vector<>(cursor.getCount());
//        if (cursor.moveToFirst()) {
//            do {
//                ContentValues cv = new ContentValues();
//                DatabaseUtils.cursorRowToContentValues(cursor, cv);
//                valuesVector.add(cv);
//            } while (cursor.moveToNext());
//        }
        Log.d(LOG_TAG, "FetchWeatherTask complete." + valuesVector.size() + " inserted");
//        String[] resultStrs=convertContentValuesToUXFormat(valuesVector);
//        return daysInfo;
//        return resultStrs;

    }

//    private String[] convertContentValuesToUXFormat(Vector<ContentValues> valuesVector) {
//        String[] resultStrs=new String[valuesVector.size()];
//        for (int i = 0; i < valuesVector.size(); i++) {
//            ContentValues contentValues=valuesVector.elementAt(i);
//            String maxAndMinTemp=formatMaxMintemp(
//                    contentValues.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP),
//                    contentValues.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
//            String dateTime=contentValues.getAsString(WeatherContract.WeatherEntry.COLUMN_DATE);
//            String des=contentValues.getAsString(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
//            resultStrs[i]=dateTime+" - "+des+" - " +maxAndMinTemp;
//        }
//        return resultStrs;
//    }

//    private String formatMaxMintemp(double maxtemp, double mintemp) {
//            String unitsType= PreferenceManager.getDefaultSharedPreferences(context).getString(
//                    context.getString(R.string.pref_temperature_Units_key),context.getString(R.string.pref_temperature_Units_defaul));
//        if (unitsType.equals("imperial")) {
//            maxtemp = maxtemp * 1.8 + 32;
//            mintemp = mintemp * 1.8 + 32;
//        } else if (!unitsType.equals("metric")) {
//            Log.d(LOG_TAG, "could find the unitType");
//        }
////        long roundedMax = Math.round(maxtemp);
////        long roundedMin = Math.round(mintemp);
////        return roundedMax + "/" + roundedMin;
//        return maxtemp + "/" + mintemp;
//    }

    public long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;

        Cursor locationCursor = context.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
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

            Uri insertedUri = context.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI, locationValues);
            locationId = ContentUris.parseId(insertedUri);
        }
        locationCursor.close();
        return locationId;
    }
}

