package com.growse.locator.locator;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrew on 02/05/14.
 */
public class LocationPoster extends AsyncTask<Location, Void, Integer> {
    private Context context;

    public LocationPoster(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Location... locations) {
        Location locationInfo = null;
        if (locations.length > 0) {
            locationInfo = locations[0];
        }
        if (locationInfo != null) {
            Log.i("LocatorNetwork", "Valid location");

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
            String ssid = wifiManager.getConnectionInfo().getSSID();
            String mobileNetworkType = getNetworkClass(context);
            nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(locationInfo.getLatitude())));
            nameValuePairs.add(new BasicNameValuePair("long", String.valueOf(locationInfo.getLongitude())));
            nameValuePairs.add(new BasicNameValuePair("acc", String.valueOf(locationInfo.getAccuracy())));
            nameValuePairs.add(new BasicNameValuePair("time", String.valueOf(locationInfo.getTime())));
            nameValuePairs.add(new BasicNameValuePair("wifissid", ssid));
            nameValuePairs.add(new BasicNameValuePair("gsmtype", mobileNetworkType));
            LocationQueue.INSTANCE.getQueue().add(nameValuePairs);
        }
        if (isConnected(context)) {
            List<NameValuePair> nameValuePair;
            while ((nameValuePair = LocationQueue.INSTANCE.getQueue().poll()) != null) {
                String endpoint;
                if (BuildConfig.DEBUG) {
                    endpoint = "https://sni.velox.ch/";
                } else {
                    endpoint = "https://www.growse.com/locator/";
                }


                HttpClient client = new DefaultHttpClient();
                SchemeRegistry schemeRegistry = client.getConnectionManager().getSchemeRegistry();
                schemeRegistry.register(new Scheme("https", new TlsSniSocketFactory(), 443));

                HttpPost post = new HttpPost(endpoint);
                try {
                    post.setEntity(new UrlEncodedFormEntity(nameValuePair));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                HttpResponse response;
                try {
                    response = client.execute(post);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        Log.i("Network", "Network post success");
                    }
                } catch (IOException e) {
                    Log.i("Network", "Network post failure, requeuing");
                    LocationQueue.INSTANCE.getQueue().addFirst(nameValuePair);
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    private boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
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
