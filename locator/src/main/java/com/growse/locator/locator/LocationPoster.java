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

    @Override
    protected Integer doInBackground(String... params) {
        Log.i(this.getClass().getName(), String.format("Attempting to post to network: %s", params[0]));

        URL endpoint;
        if (BuildConfig.DEBUG) {
            try {
                //endpoint = new URL("https://sni.velox.ch/");
                endpoint = new URL("https://www.growse.com/locator/");
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
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(params[0]);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.i(this.getClass().getName(), "Network post success");
            }
            return 0;
        } catch (IOException e) {
            Log.i(this.getClass().getName(), "Network post failure, requeuing");

            e.printStackTrace();
            return 1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
