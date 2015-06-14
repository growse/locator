package com.growse.locator.locator;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;


public class MainActivity extends Activity {
    private LocatorSystemService service;
    private LocatorSystemServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.setProperty("org.joda.time.DateTimeZone.Provider",
                "com.growse.locator.locator.JdkBasedTimeZoneProvider");
        Log.i("Locator", "Locator Activity starting");
        //startService(new Intent(MainActivity.this, LocatorSystemService.class));

        setContentView(R.layout.activity_main);
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshDisplay();
            }
        });
        Intent transmitIntent = new Intent(this, TransmitReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, transmitIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10000, 10000, pendingIntent);
        Log.i("Locator","Alarm set");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Locator", "onResume");
        connectToService();
        refreshDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Locator", "onPause");
        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    private void connectToService() {
        Log.i("Locator", "Connect To Service");
        // Calling startService() first prevents it from being killed on unbind()
        startService(new Intent(MainActivity.this, LocatorSystemService.class));

        // Now connect to it
        serviceConnection = new LocatorSystemServiceConnection();

        boolean result = bindService(
                new Intent(this, LocatorSystemService.class),
                serviceConnection,
                BIND_AUTO_CREATE
        );

        if (!result) {
            throw new RuntimeException("Unable to bind with service.");
        }
    }

    private void refreshDisplay() {
        final View locationTable = findViewById(R.id.location_table);
        final TextView locationTextView = (TextView) findViewById(R.id.location_title);
        if (service != null) {
            Location location = service.getLocation();

            if (location != null) {
                Log.i("Locator", String.valueOf(service.getLocation()));
                locationTable.setVisibility(View.VISIBLE);
                DateTime then = new DateTime(location.getTime());
                DateTime now = new DateTime();
                Period period = new Period(then, now);
                ((TextView) findViewById(R.id.location_timestamp)).setText(new DateTime(location.getTime()).toString());
                ((TextView) findViewById(R.id.location_latitude)).setText(Double.toString(location.getLatitude()));
                ((TextView) findViewById(R.id.location_longitude)).setText(Double.toString(location.getLongitude()));
                ((TextView) findViewById(R.id.location_accuracy)).setText(Float.toString(location.getAccuracy()) + "m");
                ((TextView) findViewById(R.id.location_speed)).setText(Float.toString(location.getSpeed()) + "m");
                ((TextView) findViewById(R.id.last_posted)).setText(formatter.print(period));
                ((TextView) findViewById(R.id.pending_locations)).setText("HI");
            } else {
                Log.i("Locator","location not yet set");
            }
        } else {
            Log.w("Locator","No service!");
        }

        locationTextView.setText("Latest location has been broadcast");

    }

    protected class LocatorSystemServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.i("Activity", String.format("onServiceConnected %s", className));
            service = ((LocatorSystemService.LocalBinder) binder).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e("Activity", String.format("onServiceDisconnected %s", className));
            service = null;
        }
    }


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
}

