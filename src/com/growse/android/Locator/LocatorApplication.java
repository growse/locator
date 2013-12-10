package com.growse.android.Locator;

import android.app.Application;
import android.util.Log;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

/**
 * Created by Andrew on 09/12/13.
 */
public class LocatorApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            LocationLibrary.initialiseLibrary(getBaseContext(), "com.growse.android.Locator");
        } catch (UnsupportedOperationException ex) {
            Log.d("Locator", "UnsupportedOperationException thrown - the device doesn't have any location providers");
        }
    }
}
