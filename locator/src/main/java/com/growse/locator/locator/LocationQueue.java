package com.growse.locator.locator;

import android.content.ContentValues;
import android.content.Context;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by andrew on 14/09/2014.
 */
public class LocationQueue {
    private SqliteQueue queue;

    public LocationQueue(Context context) {
        queue  = new SqliteQueue(context);
    }

    public SqliteQueue getQueue() {
        return queue;
    }
}

