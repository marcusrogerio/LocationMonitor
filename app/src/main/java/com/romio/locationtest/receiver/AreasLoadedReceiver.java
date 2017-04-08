package com.romio.locationtest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.geofence.GeofenceManager;

/**
 * Created by roman on 3/27/17
 */

public class AreasLoadedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationMonitorApp app = (LocationMonitorApp) context.getApplicationContext();
        GeofenceManager geofenceManager = app.getGeofenceManager();

        if (!geofenceManager.isGeofencing()) {
            geofenceManager.restart();
        }
    }
}
