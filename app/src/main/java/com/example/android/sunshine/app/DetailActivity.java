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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        final static String LOG_TAG = DetailFragment.class.getSimpleName();
        final static String FORECAST_SHARE_HASHTAG = "#SunShineApp";
        String mForecast;
        Cursor cursor;
        android.support.v7.widget.ShareActionProvider mShareActionProvider;
        TextView tv_dayInfo_detail;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.detailfragment, menu);
            MenuItem menuItem = menu.findItem(R.id.action_share);
            mShareActionProvider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if (mForecast != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                android.util.Log.d(LOG_TAG, "A share action is null?");
            }
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);//设置FLAG，返回时不会留在INTENT启动
            // 的APP中,FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET在API21中被弃用，用 FLAG_ACTIVITY_NEW_DOCUMENT代替
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
            shareIntent.setType("text/plain");
            return shareIntent;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
//            String forecast=savedInstanceState.getString(Intent.EXTRA_TEXT);

            return rootView;
        }

        public static final String[] DETAIL_COLUNMS = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_COORD_LON,
                WeatherContract.LocationEntry.COLUMN_CITY_NAME,};
        static final int COL_WEATHER_ID = 0;
        static final int COL_WEATHER_DATE = 1;
        static final int COL_WEATHER_DESC = 2;
        static final int COL_WEATHER_MAX_TEMP = 3;
        static final int COL_WEATHER_MIN_TEMP = 4;
        static final int COL_LOCATION_SETTING = 5;
        static final int COL_COORD_LAT = 6;
        static final int COL_COORD_LON = 7;
        static final int COL_CITYNAME = 8;
        static final int DETAIL_LOADER_ID = 2;

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                mForecast = intent.getDataString();
                return new CursorLoader(getActivity(),
                        intent.getData(),DETAIL_COLUNMS, null, null, null);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.moveToFirst()) {
                String dateTime = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
                String cityName = data.getString(COL_CITYNAME);
                String description = data.getString(COL_WEATHER_DESC);
                double lat = data.getDouble(COL_COORD_LAT);
                double lon = data.getDouble(COL_COORD_LON);
                double maxTemp = data.getDouble(COL_WEATHER_MAX_TEMP);
                double minTemp = data.getDouble(COL_WEATHER_MIN_TEMP);
                mForecast = cityName + " \n " +
                        dateTime + " \n " +
                        "Lat: " + lat + "  " + "lon: " + lon + "\n" +
                        "Temp: " + Utility.formatTemperature(maxTemp, Utility.isMetric(getActivity())) + "/" + Utility.formatTemperature(minTemp, Utility.isMetric(getActivity()))
                        + " \n "+description;
            }
            tv_dayInfo_detail = (TextView) getView().findViewById(R.id.tv_dayInfo_detail);
            tv_dayInfo_detail.setText(mForecast);
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

}