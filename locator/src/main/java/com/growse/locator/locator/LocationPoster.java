package com.growse.locator.locator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;

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
 * Created by andrew on 02/05/14.
 */
public class LocationPoster extends AsyncTask<LocationInfo, Void, Integer> {
    private Context context;

    public LocationPoster(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(LocationInfo... locations) {
        LocationInfo locationInfo = null;
        if (locations.length > 0) {
            locationInfo = locations[0];
        }
        if (isConnected(context) && locationInfo != null) {
            Log.i("LocatorNetwork", "Valid location");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://www.growse.com/locator/");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(locationInfo.lastLat)));
            nameValuePairs.add(new BasicNameValuePair("long", String.valueOf(locationInfo.lastLong)));
            nameValuePairs.add(new BasicNameValuePair("acc", String.valueOf(locationInfo.lastAccuracy)));
            nameValuePairs.add(new BasicNameValuePair("time", String.valueOf(locationInfo.lastLocationUpdateTimestamp)));
            try {
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            HttpResponse response;
            try {
                response = client.execute(post);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Log.i("Network", "Network post success");
                }
                return response.getStatusLine().getStatusCode();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        return -1;
    }

    private boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
}
