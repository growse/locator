package com.growse.locator.locator;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by andrew on 13/06/2015.
 */
public class LocatorSystemService extends Service implements GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleApiClient.ConnectionCallbacks {


    public LocationQueue getLocationQueue() {
        return locationQueue;
    }

    private LocationQueue locationQueue = new LocationQueue();
    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private String androidId;

    public Location getLocation() {
        return mLocation;
    }

    public class LocalBinder extends Binder {
        public LocatorSystemService getService() {
            return LocatorSystemService.this;
        }
    }

    private final LocalBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /***
     * Need to make sure we return START_STICKY so that the service persists
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Service", "onStartCommand");
        DoNetworkPost();
        return android.app.Service.START_STICKY;
    }

    private void DoNetworkPost() {
        if (isConnected(this)) {
            Log.i("Locator", "Network is good, going to post");
            ContentValues contentValues;
            ArrayList<ContentValues> valueList = new ArrayList<>();
            while ((contentValues = locationQueue.getQueue().poll()) != null) {
                valueList.add(contentValues);
            }
            Log.i("Locator", String.format("%d values to post", valueList.size()));
            if (valueList.size() > 0) {
                JSONArray array = new JSONArray();
                try {
                    for (ContentValues values : valueList) {
                        JSONObject obj = new JSONObject();
                        for (String key : values.keySet()) {
                            obj.put(key, values.get(key));
                        }
                        array.put(obj);
                    }
                } catch (JSONException e) {
                    Log.e("Locator", "Error encoding JSON", e);

                }
                AsyncTask<String,Void,Integer> task = new LocationPoster(this).execute(array.toString());

                int result = 0;
                try {
                    result = task.get();

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    Log.i("Locator", "Exception, Requeueing");
                    for (ContentValues values : valueList) {
                        locationQueue.getQueue().addFirst(values);
                    }
                }
                if (result != 0) {
                    Log.i("Locator", "Requeueing");
                    for (ContentValues values : valueList) {
                        locationQueue.getQueue().addFirst(values);
                    }
                }
            }
        }
    }

    private boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    /***
     * On service start, create and connect to the google api client
     */
    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.i("Locator", "Locator Service starting");
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mGoogleApiClient.connect();


    }

    /***
     * Tidy up once we're done
     */
    @Override
    public void onDestroy() {
        Log.i("Locator", "Locator Service destroy");
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(60000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Locator", "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        Log.i("Locator", String.format("New location! %f %f %f", location.getLatitude(), location.getLongitude(), location.getAccuracy()));

        Log.i("LocatorNetwork", "Valid location");

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        String mobileNetworkType = getNetworkClass(this);
        ContentValues values = new ContentValues();

        values.put("lat", String.valueOf(location.getLatitude()));
        values.put("long", String.valueOf(location.getLongitude()));
        values.put("acc", String.valueOf(location.getAccuracy()));
        values.put("time", String.valueOf(location.getTime()));
        values.put("wifissid", ssid);
        values.put("gsmtype", mobileNetworkType);
        values.put("deviceid", androidId);
        locationQueue.getQueue().add(values);
        Log.i("Locator", String.format("Queue length: %d", locationQueue.getQueue().size()));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Locator", "GoogleApiClient connection has failed");
    }

    public String getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }
}
