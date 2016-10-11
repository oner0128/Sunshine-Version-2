package com.example.android.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.service.SunShineService;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();

    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Sarting Snyc");
        HttpURLConnection httpURLConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;

//      传入LOCATION和UNIT_TYPE
        String locationSetting = Utility.getPreferredLocation(getContext());
        String unitsType = "metric";
        String type = "accurate";
        String appid = "b21e787cebb54337b23e4816da79da62";

        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
            final String POSTCODE_PARAM = "id";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";//请求预测的天气情况数目
            final String TYPE_PARAM = "type";
            final String APPID_PARAM = "APPID";

            //构建请求天气的API
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
//                Log.v(LOG_TAG, "forecast JSON string :" + forecastJsonStr);//在控制台输出返回的天气数据
            Utility.getWeatherDataFromJson(getContext(),forecastJsonStr, locationSetting);
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

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        }
        return newAccount;
    }
}