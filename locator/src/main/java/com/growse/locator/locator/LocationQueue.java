package com.growse.locator.locator;

import android.content.ContentValues;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by andrew on 14/09/2014.
 */
public class LocationQueue {
    private LinkedBlockingDeque<ContentValues> queue = new LinkedBlockingDeque<>();

    public LinkedBlockingDeque<ContentValues> getQueue() {
        return queue;
    }
}

