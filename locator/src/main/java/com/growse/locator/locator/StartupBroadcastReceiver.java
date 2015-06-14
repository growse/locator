package com.growse.locator.locator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by andrew on 13/06/2015.
 */
public class StartupBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, LocatorSystemService.class);
            context.startService(serviceIntent);
        }
    }
}
