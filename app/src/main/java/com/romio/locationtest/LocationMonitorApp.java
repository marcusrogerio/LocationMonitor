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
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.romio.locationtest.data.DBManager;
import com.romio.locationtest.data.DataBaseHelper;
import com.romio.locationtest.data.TargetAreaDto;
import com.romio.locationtest.data.TargetAreaMapper;
import com.romio.locationtest.service.LocationMonitorService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by roman on 1/9/17
 */

public class LocationMonitorApp extends Application {

    private static final String TAG = LocationMonitorApp.class.getSimpleName();
    private DataBaseHelper databaseHelper = null;
    private static final String LOCATION_MONITOR_ALARM = "com.romio.locationtest.alarm.location_monitor";
    private static final String AREA_MONITOR_ALARM = "com.romio.locationtest.alarm.area_monitor";

    private int locationMonitorOffset = 3000;
    private int locationMonitorInterval = 10000;


    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

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
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = prepareLocationMonitorPendingIntent();

        if (!isLocationMonitorAlarmSet()) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + locationMonitorOffset, locationMonitorInterval, pendingIntent);

            saveLocationMonitorAlarmWasSet(true);
            Toast.makeText(mainActivity, "Start listening for updates", Toast.LENGTH_SHORT).show();

        } else {
            alarmManager.cancel(pendingIntent);
            saveLocationMonitorAlarmWasSet(false);
            Toast.makeText(mainActivity, "Stop listening for updates", Toast.LENGTH_SHORT).show();
        }
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
        intent.putParcelableArrayListExtra(LocationMonitorService.DATA, readTargets());

        return PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void saveLocationMonitorAlarmWasSet(boolean isSet) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences
                .edit()
                .putBoolean(LOCATION_MONITOR_ALARM, isSet)
                .commit();
    }
}
