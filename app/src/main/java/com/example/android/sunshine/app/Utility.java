/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class Utility {
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
//    public static String getFriendlyDayString(Context context, long dateInMillis){
//        // The day string for forecast uses the following logic:
//        // For today: "Today, June 8"
//        // For tomorrow:  "Tomorrow"
//        // For the next 5 days: "Wednesday" (just the day name)
//        // For all days after that: "Mon Jun 8"
//
//        Time time = new Time();
//        time.setToNow();
//        long currentTime = System.currentTimeMillis();
//        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
//        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);
//
//        // If the date we're building the String for is today's date, the format
//        // is "Today, June 24"
//        if (julianDay == currentJulianDay) {
//            String today = context.getString(R.string.today);
//            int formatId = R.string.format_full_friendly_date;
//            return String.format(context.getString(formatId, today, getFormattedMonthDay(context, dateInMillis)));
//        } else if (julianDay < currentJulianDay + 7) {
//            // If the input date is less than a week in the future, just return the day name.
//            return getDayName(context, dateInMillis);
//        } else {
//            // Otherwise, use the form "Mon Jun 3"
//            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            return shortenedDateFormat.format(dateInMillis);
//        }
//        return null;
//    }
//
//    /**
//     * Given a day, returns just the name to use for that day.
//     * E.g "today", "tomorrow", "wednesday".
//     *
//     * @param context      Context to use for resource localization
//     * @param dateInMillis The date in milliseconds
//     * @return
//     */
//    public static String getDayName(Context context, long dateInMillis) {
//        // If the date is today, return the localized version of "Today" instead of the actual
//        // day name.
//
//        Time t = new Time();
//        t.setToNow();
//        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
//        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
//        if (julianDay == currentJulianDay) {
//            return context.getString(R.string.today);
//        } else if (julianDay == currentJulianDay + 1) {
//            return context.getString(R.string.tomorrow);
//        } else {
//            Time time = new Time();
//            time.setToNow();
//            // Otherwise, the format is just the day of the week (e.g "Wednesday".
//            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
//            return dayFormat.format(dateInMillis);
//        }
//    }
//
//    /**
//     * Converts db date format to the format "Month day", e.g "June 24".
//     *
//     * @param context      Context to use for resource localization
//     * @param dateInMillis The db formatted date string, expected to be of the form specified
//     *                     in Utility.DATE_FORMAT
//     * @return The day in the form of a string formatted "December 6"
//     */
//    public static String getFormattedMonthDay(Context context, long dateInMillis) {
//        Time time = new Time();
//        time.setToNow();
//        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
//        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
//        String monthDayString = monthDayFormat.format(dateInMillis);
//        return monthDayString;
//    }
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_key_Location),
                context.getString(R.string.pref_default_Location));
    }

    public static String getUnitType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_temperature_Units_key),
                context.getString(R.string.pref_temperature_Units_defaul));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_temperature_Units_key),
                context.getString(R.string.pref_temperature_Units_defaul))
                .equals(context.getString(R.string.pref_temperature_Units_defaul));
    }

    static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(long dateInSeconds) {
        Date date = new Date(dateInSeconds * 1000);
        return DateFormat.getDateTimeInstance(android.icu.text.DateFormat.SHORT, android.icu.text.DateFormat.SHORT, Locale.CHINESE).format(date);
    }

    static final int VIEW_TYPE_TODAY = 0;

    //通过weatherId获取相对应天气的图标ID，即drawable中的
    public static int getIconResourceForWeatherCondition(int weatherId, int view_type) {
        if (view_type == VIEW_TYPE_TODAY) {
            if (weatherId >= 200 && weatherId <= 232) {
                return R.drawable.art_storm;
            } else if ((weatherId >= 300 && weatherId <= 321) || (weatherId >= 520 && weatherId <= 531)) {
                return R.drawable.art_rain;
            } else if (weatherId >= 500 && weatherId <= 504) {
                return R.drawable.art_light_rain;
            } else if ((weatherId >= 600 && weatherId <= 622) || (weatherId == 511)) {
                return R.drawable.art_snow;
            } else if (weatherId >= 701 && weatherId <= 781) {
                return R.drawable.art_fog;
            } else if (weatherId == 800) {
                return R.drawable.art_clear;
            } else if (weatherId == 801) {
                return R.drawable.art_light_clouds;
            } else if (weatherId >= 802 && weatherId <= 804) {
                return R.drawable.art_clouds;
            }
        } else {
            if (weatherId >= 200 && weatherId <= 232) {
                return R.drawable.ic_storm;
            } else if ((weatherId >= 300 && weatherId <= 321) || (weatherId >= 520 && weatherId <= 531)) {
                return R.drawable.ic_rain;
            } else if (weatherId >= 500 && weatherId <= 504) {
                return R.drawable.ic_light_rain;
            } else if ((weatherId >= 600 && weatherId <= 622) || (weatherId == 511)) {
                return R.drawable.ic_snow;
            } else if (weatherId >= 701 && weatherId <= 781) {
                return R.drawable.ic_fog;
            } else if (weatherId == 800) {
                return R.drawable.ic_clear;
            } else if (weatherId == 801) {
                return R.drawable.ic_light_clouds;
            } else if (weatherId >= 802 && weatherId <= 804) {
                return R.drawable.ic_cloudy;
            }
        }
        return -1;
    }
    public static void getWeatherDataFromJson(Context context,String weatherJsonStr, String locationSetting)
            throws JSONException {
        final String LOG_TAG=context.getClass().getName();
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

        long locationId = addLocation(context,locationSetting, cityName, lat, lon);

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
            context.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, contentValues);
        }

        Log.d(LOG_TAG, "FetchWeatherTask complete." + valuesVector.size() + " inserted");
    }

    public static long addLocation(Context context,String locationSetting, String cityName, double lat, double lon) {
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