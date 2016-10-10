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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Created by hrong on 2016/10/4.
 */

public  class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    final static String LOG_TAG = DetailFragment.class.getSimpleName();
    final static String FORECAST_SHARE_HASHTAG = "#SunShineApp";
    final static String DETAIL_URI="URI";
    private Uri mUri;
    String mForecast;
    android.support.v7.widget.ShareActionProvider mShareActionProvider;
    TextView tv_dayInfo_detail;

    public void onLocationChanged(String newLocation) {
        Uri uri = mUri;
        if (uri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
        }
    }

    public static class ViewHodler {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidityView;
        public final TextView windSpeeedView;
        public final TextView pressureView;

        public ViewHodler(View view) {
            this.iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            this.dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            this.descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            this.highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            this.lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            this.humidityView = (TextView) view.findViewById(R.id.list_item_humidity_textview);
            this.windSpeeedView = (TextView) view.findViewById(R.id.list_item_windSpeed_textview);
            this.pressureView = (TextView) view.findViewById(R.id.list_item_pressure_textview);
        }
    }

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

        Bundle arguments=getArguments();
        if (arguments!=null)
            mUri=arguments.getParcelable(DetailFragment.DETAIL_URI);
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

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
            WeatherContract.LocationEntry.COLUMN_CITY_NAME,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_COORD_LAT = 6;
    static final int COL_COORD_LON = 7;
    static final int COL_CITYNAME = 8;
    static final int COL_HUMIDITY = 9;
    static final int COL_WIND_SPEED = 10;
    static final int COL_PRESSURE = 11;
    static final int COL_WEATHER_CONDITION_ID = 12;
    static final int DETAIL_LOADER_ID = 2;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(getActivity(),
                    mUri, DETAIL_COLUNMS, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data!=null&&data.moveToFirst()) {
            ViewHodler viewHodler = new ViewHodler(getView());
            boolean isMetric=Utility.isMetric(getActivity());

            int weatherId=data.getInt(COL_WEATHER_CONDITION_ID);
            String dateTime = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
            String cityName = data.getString(COL_CITYNAME);
            String description = data.getString(COL_WEATHER_DESC);
            double lat = data.getDouble(COL_COORD_LAT);
            double lon = data.getDouble(COL_COORD_LON);
            String humidity = data.getString(COL_HUMIDITY);
            String windSpeed = data.getString(COL_WIND_SPEED);
            String pressure = data.getString(COL_PRESSURE);
            String maxTemp = Utility.formatTemperature(getActivity(),data.getDouble(COL_WEATHER_MAX_TEMP),isMetric);
            String minTemp = Utility.formatTemperature(getActivity(),data.getDouble(COL_WEATHER_MIN_TEMP),isMetric);

            //imageIcon
            viewHodler.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId,0));
            //date
            viewHodler.dateView.setText(dateTime);
            //description
            viewHodler.descriptionView.setText(description);
            //high And low temp
            viewHodler.highTempView.setText(maxTemp);
            viewHodler.lowTempView.setText(minTemp);
            //HUMIDITY,windspeed,pressure
            viewHodler.humidityView.setText("humidity: "+humidity+" %");
            viewHodler.windSpeeedView.setText("windSpeed: "+windSpeed+" km/h");
            viewHodler.pressureView.setText("pressure: "+pressure+" hPa");
        }

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
