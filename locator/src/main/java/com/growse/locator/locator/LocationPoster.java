package com.growse.locator.locator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by andrew on 02/05/14.
 */
public class LocationPoster extends AsyncTask<String, Void, Integer> {
    private Context context;
    private LocatorSystemService service;
    private MainActivity.LocatorSystemServiceConnection serviceConnection;

    public LocationPoster(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(String... params) {
        Log.i("Locator", "Attempting to post to network");

        URL endpoint;
        if (BuildConfig.DEBUG) {
            try {
                endpoint = new URL("https://sni.velox.ch/");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return 1;
            }
        } else {
            try {
                endpoint = new URL("https://www.growse.com/locator/");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return 1;
            }
        }
        HttpsURLConnection conn = null;
        try {
            conn = (HttpsURLConnection) endpoint.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(params[0]);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.i("Network", "Network post success");
            }
            return 0;
        } catch (IOException e) {
            Log.i("Network", "Network post failure, requeuing");

            e.printStackTrace();
            return 1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }


        }

    }

    private boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
