package com.romio.locationtest;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

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
import com.romio.locationtest.service.LocationMonitorService;
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
    private static final String LOCATION_MONITOR_ALARM = "com.romio.locationtest.alarm.location_monitor";
    private static final String SERVICE_IS_RUNNING = "com.romio.locationtest.service.is.running";
    private static final String SERVICE_TAG = LocationUpdateJobService.class.getSimpleName();
    private static final String AREA_MONITOR_ALARM = "com.romio.locationtest.alarm.area_monitor";

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

    public void toggleLocationMonitorService(MainActivity mainActivity) {
        Driver driver = new GooglePlayDriver(getApplicationContext());
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        if (isServiceRunning()) {
            dispatcher.cancel(SERVICE_TAG);

        } else {
            Job myJob = dispatcher.newJobBuilder()
                    .setService(LocationUpdateJobService.class)
                    .setRecurring(true)
                    .setTrigger(Trigger.executionWindow(30, 30 + 30))
                    .setReplaceCurrent(true)
                    .setLifetime(Lifetime.FOREVER)
                    .setTag(SERVICE_TAG)
                    .build();

            dispatcher.mustSchedule(myJob);
        }


//        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        PendingIntent pendingIntent = prepareLocationMonitorPendingIntent();
//
//        if (!isLocationMonitorAlarmSet()) {
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + locationMonitorOffset, locationMonitorInterval, pendingIntent);
//
//            saveLocationMonitorAlarmWasSet(true);
//            Toast.makeText(mainActivity, "Start listening for updates", Toast.LENGTH_SHORT).show();
//
//        } else {
//            alarmManager.cancel(pendingIntent);
//            saveLocationMonitorAlarmWasSet(false);
//            Toast.makeText(mainActivity, "Stop listening for updates", Toast.LENGTH_SHORT).show();
//        }
    }

    public boolean isLocationMonitorAlarmSet() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(LOCATION_MONITOR_ALARM, false);
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

    private PendingIntent prepareLocationMonitorPendingIntent() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.setAction(AlarmReceiver.START_LOCATION_MONITOR);

        return PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE, intent, 0);
    }

    private void saveLocationMonitorAlarmWasSet(boolean isSet) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences
                .edit()
                .putBoolean(LOCATION_MONITOR_ALARM, isSet)
                .commit();
    }

    private void toggleSeriveIsRunning(boolean isRunning) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences
                .edit()
                .putBoolean(SERVICE_IS_RUNNING, isRunning)
                .commit();
    }

    private boolean isServiceRunning() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(SERVICE_IS_RUNNING, false);
    }
}
