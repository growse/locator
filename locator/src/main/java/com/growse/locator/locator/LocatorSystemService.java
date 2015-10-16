package com.growse.locator.locator;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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

    private LocationQueue locationQueue = new LocationQueue(this);
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
        Log.d(this.getClass().getName(), "onStartCommand");
        DoNetworkPost();
        return android.app.Service.START_STICKY;
    }

    private void DoNetworkPost() {
        if (isConnected(this)) {
            Log.d(this.getClass().getName(), "Network is good, going to POST to network");
            String contentValues;
            ArrayList<String> valueList = new ArrayList<>();
            while ((contentValues = locationQueue.getQueue().poll()) != null) {
                valueList.add(contentValues);
            }
            Log.d(this.getClass().getName(), String.format("%d values to post", valueList.size()));
            if (valueList.size() > 0) {
                JSONArray array = new JSONArray();
                try {
                    for (String locationObjJSON : valueList) {
                        JSONObject obj = new JSONObject(locationObjJSON);
                        array.put(obj);
                    }
                } catch (JSONException e) {
                    Log.e(this.getClass().getName(), "Error encoding JSON", e);

                }
                AsyncTask<String, Void, Integer> task = new LocationPoster().execute(array.toString());

                int result = 0;
                try {
                    result = task.get();

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    Log.i(this.getClass().getName(), "Exception, Requeueing");
                    for (String value : valueList) {
                        locationQueue.getQueue().add(value);
                    }
                }
                if (result != 0) {
                    Log.i(this.getClass().getName(), "Network Status bad. Requeueing");
                    for (String value : valueList) {
                        locationQueue.getQueue().add(value);
                    }
                }
            }
        } else {
            Log.i(this.getClass().getName(), "No network, skipping posting");
        }
    }

    public int getLocationQueueSize() {
        return locationQueue.getQueue().size();
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
        Log.i(this.getClass().getName(), "Locator Service starting");
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mGoogleApiClient.connect();


    }

    /***
     * Tidy up once we're done
     */
    @Override
    public void onDestroy() {
        Log.i(this.getClass().getName(), "Locator Service destroy");
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(60000); // Update location every second
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            Log.e("Location permissions", "Location permission has been revoked.");
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Locator", "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        Log.i(this.getClass().getName(), String.format("New location! %f %f %f", location.getLatitude(), location.getLongitude(), location.getAccuracy()));

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        String mobileNetworkType = getNetworkClass(this);
        JSONObject values = new JSONObject();
        try {
            values.put("lat", location.getLatitude());
            values.put("long", location.getLongitude());
            values.put("acc", location.getAccuracy());
            values.put("time", location.getTime());
            values.put("wifissid", ssid);
            values.put("gsmtype", mobileNetworkType);
            values.put("deviceid", androidId);
            locationQueue.getQueue().add(values.toString());
            Log.d(this.getClass().getName(), String.format("Queue length: %d", locationQueue.getQueue().size()));
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "Error encoding JSON", e);
        }
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.sendBroadcast(new Intent("locationReceived"));
        DoNetworkPost();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(this.getClass().getName(), "GoogleApiClient connection has failed");

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
