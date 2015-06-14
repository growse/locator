package com.growse.locator.locator;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by andrew on 13/06/2015.
 */
public class LocatorSystemService extends Service implements GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleApiClient.ConnectionCallbacks {
    private LocationRequest mLocationRequest;
    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;

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
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Service", "onStartCommand");
        return android.app.Service.START_STICKY;
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
        mLocationRequest = LocationRequest.create();
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
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Locator", "GoogleApiClient connection has failed");
    }
}
