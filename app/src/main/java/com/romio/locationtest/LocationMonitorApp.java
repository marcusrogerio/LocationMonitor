package com.romio.locationtest;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.romio.locationtest.data.DBManager;
import com.romio.locationtest.data.DataBaseHelper;
import com.romio.locationtest.data.TargetAreaDto;
import com.romio.locationtest.data.TargetAreaMapper;
import com.romio.locationtest.service.LocationUpdateJobService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by roman on 1/9/17
 */

public class LocationMonitorApp extends Application {

    public static final String TAG = LocationMonitorApp.class.getSimpleName();
    private DataBaseHelper databaseHelper = null;
    private static final String SERVICE_IS_RUNNING = "com.romio.locationtest.service.is.running";
    private static final String SERVICE_TAG = LocationUpdateJobService.class.getSimpleName();

    private int locationMonitorOffset = 3000;
    private int locationMonitorInterval = 10000;


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        locationMonitorOffset = getResources().getInteger(R.integer.location_monitor_time_offset);
        locationMonitorInterval = getResources().getInteger(R.integer.location_monitor_time_interval);
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

    public void toggleLocationMonitorService() {
        Driver driver = new GooglePlayDriver(getApplicationContext());
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        if (isServiceRunning()) {

            dispatcher.cancelAll();
            setServiceIsRunning(false);

        } else {
            int intervalInSeconds = locationMonitorInterval / 1000;

            Job myJob = dispatcher.newJobBuilder()
                    .setService(LocationUpdateJobService.class)
                    .setRecurring(true)
                    .setTrigger(Trigger.executionWindow(30, intervalInSeconds))
                    .setReplaceCurrent(true)
                    .setLifetime(Lifetime.FOREVER)
                    .setTag(SERVICE_TAG)
                    .build();

            dispatcher.mustSchedule(myJob);
            setServiceIsRunning(true);
        }
    }

    public ArrayList<TargetArea> readTargets() {
        DBManager dbManager = getDBManager();
        try {
            List<TargetAreaDto> targetAreaDtoList = dbManager.getAreaDao().queryForAll();
            return TargetAreaMapper.mapFromDto(targetAreaDtoList);

        } catch (SQLException e) {
            Log.e(TAG, "Error reading targets from DB", e);
            throw new RuntimeException(e);
        }
    }

    public boolean isServiceRunning() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(SERVICE_IS_RUNNING, false);
    }

    private void setServiceIsRunning(boolean isRunning) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences
                .edit()
                .putBoolean(SERVICE_IS_RUNNING, isRunning)
                .commit();
    }
}
