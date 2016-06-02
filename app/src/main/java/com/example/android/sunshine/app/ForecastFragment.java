package com.example.android.sunshine.app;

/**
 * Created by hrong on 2016/4/22.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    ListView listView;
    EditText et_Postcode;
    static ArrayAdapter<String> forecastAdapter;


    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//        String[] forecastArray = {
//                "Today - Sunny - 88/63", "Today - Rain - 70/59",
//                "Tomorrow - Sunny - 88/63", "Weds - Foggy - 70/40",
//                "Fir - Cloudy - 88/63", "Tus -Heavy Rain - 65/56",
//                "Sun - Sunny - 88/63", "Sat - HELP TRAPPED IN WEATHERSTATION - 60/51"};
//        List<String> weekForecast = new ArrayList<>(Arrays.asList(forecastArray));
        List<String> weekForecast = new ArrayList<>();
        listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        forecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, weekForecast);
        listView.setAdapter(forecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String forecast = forecastAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            updateWeather();
        }
        if (item.getItemId() == R.id.action_map) {
            startActivity(new Intent());
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences pref_general = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = pref_general.getString(getString(R.string.pref_key_Location), getString(R.string.pref_default_Location));
        weatherTask.execute(location);
    }

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {


        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

//            String format = "xml";
            String units = "metric";
            String type = "accurate";
            String appid = "b21e787cebb54337b23e4816da79da62";
            int numdays = 24;

            try {
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
                final String POSTCODE_PARAM = "id";
//                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String TYPE_PARAM = "type";
                final String APPID_PARAM = "APPID";

                Uri.Builder buildUri = Uri.parse(FORECAST_BASE_URL).buildUpon().
                        appendQueryParameter(POSTCODE_PARAM, params[0]).
//                        appendQueryParameter(FORMAT_PARAM, format).
        appendQueryParameter(UNITS_PARAM, units).
                                appendQueryParameter(DAYS_PARAM, Integer.toString(numdays)).
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
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                Log.e(LOG_TAG, "ERROR", e);
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                        return getWeatherDataFromJson(forecastJsonStr, numdays);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "ERROR Closing reader", e);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if (strings != null) {
                forecastAdapter.clear();
//                for (String s : strings) {
//                    forecastAdapter.add(s);
//                }
                forecastAdapter.addAll(strings);
            }
        }
    }

    private String[] getWeatherDataFromJson(String weatherJsonStr, int numdays) throws JSONException {
        final String JSON_LIST = "list";
        final String JSON_date = "dt_txt";
        final String JSON_MAIN = "main";
        final String JSON_TEMP = "temp";
        final String JSON_CITY = "city";
        final String JSON_CITYNAME = "name";
        final String JSON_WEATHER = "weather";
        final String JSON_DESCRIPTION = "description";
        final String JSON_TEMP_MAX = "temp_max";
        final String JSON_TEMP_MIN = "temp_min";
        JSONObject weatherObject = new JSONObject(weatherJsonStr);
        JSONArray days = weatherObject.getJSONArray(JSON_LIST);
        JSONObject city = weatherObject.getJSONObject(JSON_CITY);
        String[] daysInfo = new String[numdays];
        String cityname = city.getString(JSON_CITYNAME);
        String unitType = PreferenceManager.getDefaultSharedPreferences(getActivity()).
                getString(getString(R.string.pref_temperature_Units_key), getString(R.string.pref_temperature_Units_defaul));
        for (int i = 0; i < days.length(); i++) {
            JSONObject dayInfo = days.getJSONObject(i);

            String date = dayInfo.getString(JSON_date);
            String weather = dayInfo.getJSONArray(JSON_WEATHER).getJSONObject(0).getString(JSON_DESCRIPTION);

            JSONObject temperatureInfo = dayInfo.getJSONObject(JSON_MAIN);
            double temp_max = temperatureInfo.getDouble(JSON_TEMP_MAX);
            double temp_min = temperatureInfo.getDouble(JSON_TEMP_MIN);
            String daystr = date + " - " + weather + " - " + getFormatMaxMintemp(temp_max, temp_min,unitType);
            daysInfo[i] = cityname + " - " + daystr;
        }
//        for (String s : daysInfo) {
//            Log.v(LOG_TAG, "Forecast entry: " + s);
//        }

        return daysInfo;

    }

    private String getFormatMaxMintemp(double maxtemp, double mintemp, String unitTpye) {

        if (unitTpye.equals("imperial")) {
            maxtemp = maxtemp * 1.8 + 32;
            mintemp = mintemp * 1.8 + 32;
        } else if(!unitTpye.equals("metric")){
            Log.d(LOG_TAG, "could find the unitType");
        }
        long roundedMax = Math.round(maxtemp);
        long roundedMin = Math.round(mintemp);
        return roundedMax + "/" + roundedMin;
    }
}
