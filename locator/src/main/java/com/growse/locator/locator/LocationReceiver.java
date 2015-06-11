package com.growse.locator.locator;

/**
 * Created by andrew on 02/05/14.
 */
/*public class LocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Locator", "Received new location from library");
        final LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
        LocationPoster poster = new LocationPoster(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            poster.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, locationInfo);
        else
            poster.execute(locationInfo);

    }
}
*/