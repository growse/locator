package com.growse.locator.locator;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;


public class MainActivity extends Activity {
    private LocatorSystemService service;
    private LocatorSystemServiceConnection serviceConnection;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    connectToService();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Locator", "Locator Activity starting");

        setContentView(R.layout.activity_main);
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshDisplay();
            }
        });

        Integer resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                //This dialog will help the user update to the latest GooglePlayServices
                dialog.show();
            }
        } else {
            initializeServiceAlarms();
        }
    }

    private void checkPermissionsBeforeConnectingToService() {
        int hasLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Location permission required.", Toast.LENGTH_LONG).show();
                //Previously denied
            } else {
                //Ask
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

        } else {
            connectToService();
        }
    }

    private void initializeServiceAlarms() {
        Intent serviceStartIntent = new Intent(this, ServiceStartReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, serviceStartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(), 10000, pendingIntent);
        Log.i(this.getLocalClassName(), "Alarm set");
        BroadcastReceiver locationChangedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshDisplay();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(locationChangedBroadcastReceiver, new IntentFilter("locationReceived"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(this.getLocalClassName(), "onResume");
        checkPermissionsBeforeConnectingToService();
        refreshDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(this.getLocalClassName(), "onPause");
        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    private void connectToService() {
        Log.i(this.getLocalClassName(), "Connect To Service");
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
                Log.i(this.getLocalClassName(), String.valueOf(service.getLocation()));
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
                ((TextView) findViewById(R.id.pending_locations)).setText(String.valueOf(service.getLocationQueueSize()));
                locationTextView.setText("Location found.");

            } else {
                locationTextView.setText("No location yet.");
            }
        } else {
            locationTextView.setText("Couldn't connect to the location service.");
        }
    }

    protected class LocatorSystemServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.i(this.getClass().getName(), String.format("onServiceConnected %s", className));
            service = ((LocatorSystemService.LocalBinder) binder).getService();
            refreshDisplay();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(this.getClass().getName(), String.format("onServiceDisconnected %s", className));
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

