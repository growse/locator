package com.growse.locator.locator;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;


public class MainActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLocation;

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
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.i("Locator", "Locator Activity starting");
        setContentView(R.layout.activity_main);
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshDisplay();
            }
        });

        findViewById(R.id.forcelocation).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //LocationLibrary.forceLocationUpdate(MainActivity.this);
                Toast.makeText(getApplicationContext(), "Forcing a location update", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDisplay();
        //final IntentFilter lftIntentFilter = new IntentFilter(LocationLibraryConstants.getLocationChangedPeriodicBroadcastAction());
        //registerReceiver(lftBroadcastReceiver, lftIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregisterReceiver(lftBroadcastReceiver);
    }


    private void refreshDisplay() {
        final View locationTable = findViewById(R.id.location_table);
        final TextView locationTextView = (TextView) findViewById(R.id.location_title);
        if (mLocation != null) {
            locationTable.setVisibility(View.VISIBLE);
            DateTime then = new DateTime(mLocation.getTime());
            DateTime now = new DateTime();
            Period period = new Period(then, now);
            ((TextView) findViewById(R.id.location_timestamp)).setText(Long.toString(mLocation.getTime()));
            ((TextView) findViewById(R.id.location_latitude)).setText(Double.toString(mLocation.getLatitude()));
            ((TextView) findViewById(R.id.location_longitude)).setText(Double.toString(mLocation.getLongitude()));
            ((TextView) findViewById(R.id.location_accuracy)).setText(Float.toString(mLocation.getAccuracy()) + "m");
            ((TextView) findViewById(R.id.last_posted)).setText(formatter.print(period));
        }

        locationTextView.setText("Latest location has been broadcast");

    }

   /* private final BroadcastReceiver lftBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // extract the location info in the broadcast
            final LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
            // refresh the display with it
            refreshDisplay(locationInfo);
        }
    };*/

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
        refreshDisplay();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Locator", "GoogleApiClient connection has failed");
    }
}

