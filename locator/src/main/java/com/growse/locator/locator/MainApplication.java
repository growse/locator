package com.growse.locator.locator;

import android.app.Application;
import android.util.Log;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

/**
 * Created by andrew on 02/05/14.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Log.i("Locator", "Initializing fluffy library");
            LocationLibrary.showDebugOutput(true);
            LocationLibrary.initialiseLibrary(getBaseContext(), 60000, 120000, true, "com.growse.locator.locator");
        } catch (UnsupportedOperationException ex) {
            Log.d("Locator", "UnsupportedOperationException thrown - the device doesn't have any location providers");
        }
    }

}
