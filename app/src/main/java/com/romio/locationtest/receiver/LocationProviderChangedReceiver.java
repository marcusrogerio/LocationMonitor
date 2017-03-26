package com.romio.locationtest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.geofence.GeofenceManager;
import com.romio.locationtest.utils.GoogleAPIHelper;
import com.romio.locationtest.utils.GoogleAPIHelperCallback;
import com.romio.locationtest.utils.LocationUtils;

/**
 * Created by roman on 3/26/17
 */

public class LocationProviderChangedReceiver extends BroadcastReceiver {

    private static final String TAG = BootCompletedIntentReceiver.class.getSimpleName();
    private GeofenceManager geofenceManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isLocationEnabled = LocationUtils.isLocationEnabled(context);
        if (isLocationEnabled) {
            LocationMonitorApp app = (LocationMonitorApp) context.getApplicationContext();
            geofenceManager = app.getGeofenceManager();

            if (geofenceManager.isGeofencing()) {
                new GoogleAPIHelper(googleAPIHelperCallback).buildApiClient(context);
            }
        }
    }

    private GoogleAPIHelperCallback googleAPIHelperCallback = new GoogleAPIHelperCallback() {
        @Override
        public void onError(String errorMessage) {
            Log.e(TAG, errorMessage);
        }

        @Override
        public void onConnected() {
            initGeofencing();
        }

        @Override
        public void onConnectionSuspended() { }
    };

    private void initGeofencing() {
        geofenceManager.startGeofencingAfterLocationSettingsChanged();
    }
}
