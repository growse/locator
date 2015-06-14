package com.growse.locator.locator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * We need to start the service on boot
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
