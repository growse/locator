package com.growse.locator.locator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by andrew on 14/06/2015.
 */
public class ServiceStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Locator", "Transmit Receiver fired");
        context.startService(new Intent(context, LocatorSystemService.class));
    }
}
