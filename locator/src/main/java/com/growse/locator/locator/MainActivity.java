package com.growse.locator.locator;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;


public class MainActivity extends Activity {

    LocationInfo latestInfo;
    SimpleDateFormat sdfSource = new SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss");
    PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendYears().appendSuffix(" year, ", " years, ")
            .appendMonths().appendSuffix(" month, ", " months, ")
            .appendWeeks().appendSuffix(" week, ", " weeks, ")
            .appendDays().appendSuffix(" day, ", " days, ")
            .appendHours().appendSuffix(" hour, ", " hours, ")
            .appendMinutes().appendSuffix(" minute, ", " minutes, ")
            .appendSeconds().appendSuffix(" second, ", " seconds")
            .printZeroNever()
            .toFormatter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.setProperty("org.joda.time.DateTimeZone.Provider",
                "com.growse.locator.locator.JdkBasedTimeZoneProvider");
        if (latestInfo == null) {
            latestInfo = new LocationInfo(getBaseContext());
        }
        Log.i("Locator", "Locator Activity starting");
        setContentView(R.layout.activity_main);
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshDisplay();
            }
        });
        findViewById(R.id.forcelocation).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LocationLibrary.forceLocationUpdate(MainActivity.this);
                Toast.makeText(getApplicationContext(), "Forcing a location update", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        final IntentFilter lftIntentFilter = new IntentFilter(LocationLibraryConstants.getLocationChangedPeriodicBroadcastAction());
        registerReceiver(lftBroadcastReceiver, lftIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(lftBroadcastReceiver);
    }

    private void refreshDisplay() {
        refreshDisplay(new LocationInfo(this));
    }


    private void refreshDisplay(final LocationInfo locationInfo) {
        final View locationTable = findViewById(R.id.location_table);
        final TextView locationTextView = (TextView) findViewById(R.id.location_title);
        if (locationInfo.anyLocationDataReceived()) {
            locationTable.setVisibility(View.VISIBLE);
            DateTime then = new DateTime(latestInfo.lastLocationUpdateTimestamp);
            DateTime now = new DateTime();
            Period period = new Period(then, now);
            ((TextView) findViewById(R.id.location_timestamp)).setText(LocationInfo.formatTimeAndDay(locationInfo.lastLocationUpdateTimestamp, true));
            ((TextView) findViewById(R.id.location_latitude)).setText(Float.toString(locationInfo.lastLat));
            ((TextView) findViewById(R.id.location_longitude)).setText(Float.toString(locationInfo.lastLong));
            ((TextView) findViewById(R.id.location_accuracy)).setText(Integer.toString(locationInfo.lastAccuracy) + "m");
            ((TextView) findViewById(R.id.last_posted)).setText(formatter.print(period));

            if (locationInfo.hasLatestDataBeenBroadcast()) {
                locationTextView.setText("Latest location has been broadcast");
            } else {
                locationTextView.setText("Location broadcast pending (last " + LocationInfo.formatTimeAndDay(locationInfo.lastLocationUpdateTimestamp, true) + ")");
            }
        } else {
            locationTable.setVisibility(View.GONE);
            locationTextView.setText("No locations recorded yet");
        }
    }

    private final BroadcastReceiver lftBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // extract the location info in the broadcast
            final LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
            // refresh the display with it
            refreshDisplay(locationInfo);
        }
    };
}

