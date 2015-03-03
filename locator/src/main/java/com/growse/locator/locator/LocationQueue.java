package com.growse.locator.locator;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by andrew on 14/09/2014.
 */
public enum LocationQueue {
    INSTANCE;
    private LinkedBlockingDeque<List<NameValuePair>> queue = new LinkedBlockingDeque<>();

    public LinkedBlockingDeque<List<NameValuePair>> getQueue() {
        return queue;
    }
}

