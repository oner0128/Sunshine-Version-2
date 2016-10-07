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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

    static String formatTemperature(Context context,double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return context.getString(R.string.format_temperature,temp);
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
}