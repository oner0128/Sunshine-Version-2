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
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {
    private String mLocation;
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocation = Utility.getPreferredLocation(this);
        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG);
            }

        } else {mTwoPane = false;
        getSupportActionBar().setElevation(0f);}//去除操作栏和Today选项的阴影
        ForecastFragment forecastFragment= (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(mTwoPane);
        SunshineSyncAdapter.initializeAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        if (!mLocation.equals(location) && location != null) {
            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (forecastFragment != null)
                forecastFragment.onLocationChanged();

            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (detailFragment != null)
                detailFragment.onLocationChanged(location);
        }
        mLocation = location;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, dateUri);
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.weather_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(dateUri);
            startActivity(intent);
        }
    }
}
