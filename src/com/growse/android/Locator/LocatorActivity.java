package com.growse.android.Locator;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocatorActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.setProperty("org.joda.time.DateTimeZone.Provider",
                "com.growse.android.Locator.JdkBasedTimeZoneProvider");
        if (latestInfo == null) {
            latestInfo = new LocationInfo(getBaseContext());
        }
        Log.i("Locator", "Locator Activity starting");
        setContentView(R.layout.main);
        UpdateText();
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateText();

    }

    private void UpdateText() {
        TextView t = (TextView) findViewById(R.id.textView);
        DateTime then = new DateTime(latestInfo.lastLocationUpdateTimestamp);
        DateTime now = new DateTime();

        Period period = new Period(then, now);
        t.setText(String.format("Locator is running\nLast updated %s ago", formatter.print(period)));
    }

    public void UpdateLocation(View view) {
        LocationLibrary.forceLocationUpdate(this);
    }
}
