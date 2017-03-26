package com.romio.locationtest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.geofence.GeofenceManager;
import com.romio.locationtest.utils.GoogleAPIHelper;
import com.romio.locationtest.utils.GoogleAPIHelperCallback;

/**
 * Created by roman on 3/26/17
 */

public class BootCompletedIntentReceiver extends BroadcastReceiver {

    private static final String TAG = BootCompletedIntentReceiver.class.getSimpleName();
    private GeofenceManager geofenceManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationMonitorApp app = (LocationMonitorApp) context.getApplicationContext();
        geofenceManager = app.getGeofenceManager();

        if (geofenceManager.isGeofencing()) {
            new GoogleAPIHelper(googleAPIHelperCallback).buildApiClient(context);
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
        geofenceManager.startGeofencingAfterReboot();
    }
}
