package com.growse.android.Locator;

import org.joda.time.DateTimeZone;
import org.joda.time.tz.Provider;

import java.util.*;

/**
 * Created by Andrew on 10/12/13.
 */
public class JdkBasedTimeZoneProvider implements Provider {
    public static final Set<String> AVAILABLE_IDS = new HashSet<String>();

    static {
        AVAILABLE_IDS.addAll(Arrays.asList(TimeZone.getAvailableIDs()));
    }

    public DateTimeZone getZone(String id) {
        if (id == null) {
            return DateTimeZone.UTC;
        }

        TimeZone tz = TimeZone.getTimeZone(id);
        if (tz == null) {
            return DateTimeZone.UTC;
        }

        int rawOffset = tz.getRawOffset();

        //sub-optimal. could be improved to only create a new Date every few minutes
        if (tz.inDaylightTime(new Date())) {
            rawOffset += tz.getDSTSavings();
        }

        return DateTimeZone.forOffsetMillis(rawOffset);
    }

    public Set getAvailableIDs() {
        return AVAILABLE_IDS;
    }
}