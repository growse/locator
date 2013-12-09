package com.growse.android.Locator;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 09/12/13.
 */
public class LocatorService extends Service {
    // Acquire a reference to the system Location Manager
    Location previousLocation = null;
    Location pendingLocation = null;
    LocationManager locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

    public IBinder onBind(Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DoLocatorPoll();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                DoNetworkPoll();
            }
        }).start();
        return null;
    }

    private void DoNetworkPoll() {
        Criteria locationCriteria = new Criteria();
        while (true) {
            SystemClock.sleep(30000);
            Log.d("Network", "Waking to post network");
            String locationProvider = locationManager.getBestProvider(locationCriteria, true);
            if (locationProvider != null) {
                locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
            }
        }
    }


    private void PostLocation() {
        if (pendingLocation != null && isConnected()) {
            Log.d("Network", "Valid location");
            synchronized (pendingLocation) {
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://www.growse.com/locator/");
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(pendingLocation.getLatitude())));
                nameValuePairs.add(new BasicNameValuePair("long", String.valueOf(pendingLocation.getLongitude())));
                nameValuePairs.add(new BasicNameValuePair("acc", String.valueOf(pendingLocation.getAccuracy())));
                nameValuePairs.add(new BasicNameValuePair("speed", String.valueOf(pendingLocation.getSpeed())));
                nameValuePairs.add(new BasicNameValuePair("time", String.valueOf(pendingLocation.getTime())));
                try {
                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                HttpResponse response;
                try {
                    response = client.execute(post);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        Log.d("Network", "Network post success");
                        pendingLocation = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private void DoLocatorPoll() {
        Criteria locationCriteria = new Criteria();
        while (true) {
            SystemClock.sleep(30000);
            Log.d("Location", "Waking to get location");
            String locationProvider = locationManager.getBestProvider(locationCriteria, true);
            if (locationProvider != null) {
                locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
            }
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("Location", "Got location changed");
            if (isBetterLocation(location, previousLocation)) {
                synchronized (pendingLocation) {
                    Log.d("Location", "Location is new and awesome");
                    previousLocation = location;
                    pendingLocation = location;
                }
            }
            locationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
