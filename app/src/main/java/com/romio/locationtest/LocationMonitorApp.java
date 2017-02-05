package com.romio.locationtest;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.romio.locationtest.data.DBManager;
import com.romio.locationtest.data.DataBaseHelper;
import com.romio.locationtest.geofence.GeofenceManager;
import com.romio.locationtest.geofence.GeofenceManagerImpl;

import io.fabric.sdk.android.Fabric;

/**
 * Created by roman on 1/9/17
 */

public class LocationMonitorApp extends Application {

    public static final String TAG = LocationMonitorApp.class.getSimpleName();

    private DataBaseHelper databaseHelper = null;

    private GoogleApiClient googleApiClient;
    private GeofenceManager geofenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }

    public void releaseDBManager() {
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    public DBManager getDBManager() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DataBaseHelper.class);
        }

        return databaseHelper;
    }

    public GeofenceManager getGeofenceManager() {
        if (geofenceManager == null) {
            geofenceManager = new GeofenceManagerImpl(getApplicationContext(), getDBManager());
        }

        return geofenceManager;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }
}
