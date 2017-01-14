package com.romio.locationtest;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.romio.locationtest.data.DBManager;
import com.romio.locationtest.data.DataBaseHelper;

import io.fabric.sdk.android.Fabric;

/**
 * Created by roman on 1/9/17.
 */

public class LocationMonitorApp extends Application {

    private DataBaseHelper databaseHelper = null;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
    }

    public void reseaseDBManager() {
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
}
