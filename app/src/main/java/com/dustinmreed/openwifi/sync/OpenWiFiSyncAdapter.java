package com.dustinmreed.openwifi.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.dustinmreed.openwifi.MainActivity;
import com.dustinmreed.openwifi.R;
import com.dustinmreed.openwifi.data.WifiLocationContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class OpenWiFiSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final int SYNC_INTERVAL = 60 * 1440 * 3;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final int OPEN_WIFI_NOTIFICATION_ID = 5234;

    public final String LOG_TAG = OpenWiFiSyncAdapter.class.getSimpleName();

    public OpenWiFiSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        OpenWiFiSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String wifiJsonStr;

        try {
            final String FORECAST_BASE_URL = "https://data.nashville.gov/resource/4ugp-s85t.json";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon().build();

            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();


            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                builder
                        .append(line)
                        .append("\n");
            }

            if (builder.length() == 0) {
                return;
            }
            wifiJsonStr = builder.toString();
            getLocationDataFromJson(wifiJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void getLocationDataFromJson(String wifiJsonStr) throws JSONException {

        final String WIFIDATA_MAPPEDLOCTION = "mapped_location";
        final String WIFIDATA_LONG = "longitude";
        final String WIFIDATA_LAT = "latitude";
        final String WIFIDATA_HUMANADDRESS = "human_address";
        final String WIFIDATA_TYPE = "site_type";
        final String WIFIDATA_STREET_ADDRESS = "street_address";
        final String WIFIDATA_NAME = "site_name";
        final String WIFIDATA_ADDRESS = "address";
        final String WIFIDATA_CITY = "city";
        final String WIFIDATA_STATE = "state";
        final String WIFIDATA_ZIPCODE = "zip";

        try {
            JSONArray jArray = new JSONArray(wifiJsonStr);

            Vector<ContentValues> cVVector = new Vector<>(jArray.length());

            for (int i = 0; i < jArray.length(); i++) {
                String name;
                String type;
                String street_address;
                String longitude;
                String latitude;
                String address;
                String city;
                String state;
                String zipcode;

                JSONObject wifiLocation = jArray.getJSONObject(i);
                JSONObject mappedLocation = wifiLocation.getJSONObject(WIFIDATA_MAPPEDLOCTION);
                JSONObject humanAddress = new JSONObject(mappedLocation.getString(WIFIDATA_HUMANADDRESS));

                name = wifiLocation.getString(WIFIDATA_NAME);
                type = wifiLocation.getString(WIFIDATA_TYPE);
                street_address = wifiLocation.getString(WIFIDATA_STREET_ADDRESS);
                longitude = mappedLocation.getString(WIFIDATA_LONG);
                latitude = mappedLocation.getString(WIFIDATA_LAT);
                address = humanAddress.getString(WIFIDATA_ADDRESS);
                city = humanAddress.getString(WIFIDATA_CITY);
                state = humanAddress.getString(WIFIDATA_STATE);
                zipcode = humanAddress.getString(WIFIDATA_ZIPCODE);


                ContentValues wifiLocationValues = new ContentValues();

                wifiLocationValues.put(WifiLocationContract.WiFiLocationEntry.COLUMN_SITE_NAME, name);
                wifiLocationValues.put(WifiLocationContract.WiFiLocationEntry.COLUMN_SITE_TYPE, type);
                wifiLocationValues.put(WifiLocationContract.WiFiLocationEntry.COLUMN_STREET_ADDRESS, street_address);
                wifiLocationValues.put(WifiLocationContract.WiFiLocationEntry.COLUMN_COORD_LONG, longitude);
                wifiLocationValues.put(WifiLocationContract.WiFiLocationEntry.COLUMN_COORD_LAT, latitude);
                wifiLocationValues.put(WifiLocationContract.WiFiLocationEntry.COLUMN_ADDRESS, address);
                wifiLocationValues.put(WifiLocationContract.WiFiLocationEntry.COLUMN_CITY, city);
                wifiLocationValues.put(WifiLocationContract.WiFiLocationEntry.COLUMN_STATE, state);
                wifiLocationValues.put(WifiLocationContract.WiFiLocationEntry.COLUMN_ZIPCODE, zipcode);

                cVVector.add(wifiLocationValues);
            }

            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(WifiLocationContract.WiFiLocationEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");
            notifyWeather();
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void notifyWeather() {
        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if (displayNotifications) {

            int iconId = R.mipmap.ic_launcher;
            Resources resources = context.getResources();
            Bitmap largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);
            String title = context.getString(R.string.app_name);

            String contentText = context.getString(R.string.sync_complete);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getContext())
                            .setColor(resources.getColor(R.color.primaryColor))
                            .setSmallIcon(iconId)
                            .setLargeIcon(largeIcon)
                            .setContentTitle(title)
                            .setContentText(contentText);

            Intent resultIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(OPEN_WIFI_NOTIFICATION_ID, mBuilder.build());
        }
    }
}