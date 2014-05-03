package com.growse.locator.locator;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;


public class MainActivity extends ActionBarActivity {

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
        UpdateText();
    }

    private void UpdateText() {
        TextView t = (TextView) findViewById(R.id.yay);
        DateTime then = new DateTime(latestInfo.lastLocationUpdateTimestamp);
        DateTime now = new DateTime();

        Period period = new Period(then, now);
        t.setText(String.format("Locator is running\nLast updated %s ago", formatter.print(period)));
    }

    public void UpdateLocation(View view)
    {
        Log.i("Locator", "Locator updated forced");
        LocationLibrary.forceLocationUpdate(this);
    }
}
