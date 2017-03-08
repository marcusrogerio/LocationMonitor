package com.romio.locationtest;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.romio.locationtest.data.AreasManager;
import com.romio.locationtest.data.AreasManagerImpl;
import com.romio.locationtest.data.TrackingManager;
import com.romio.locationtest.data.TrackingManagerImpl;
import com.romio.locationtest.data.net.entity.TrackingEntity;
import com.romio.locationtest.service.LocationMonitorService;
import com.romio.locationtest.utils.NetworkManager;
import com.romio.locationtest.utils.NetworkManagerImpl;

import io.fabric.sdk.android.Fabric;

/**
 * Created by roman on 1/9/17
 */

public class LocationMonitorApp extends Application {

    public static final String TAG = LocationMonitorApp.class.getSimpleName();
    private static final String LOCATION_MONITOR_ALARM = "com.romio.locationtest.alarm.location_monitor";

    private AreasManager areasManager;
    private TrackingManager trackingManager;
    private NetworkManager networkManager;

    private int locationMonitorOffset = 3000;
    private int locationMonitorInterval = 120000;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        locationMonitorOffset = getResources().getInteger(R.integer.location_monitor_time_offset);
        locationMonitorInterval = getResources().getInteger(R.integer.location_monitor_time_interval);
    }

    public void toggleLocationMonitorService(MainActivity mainActivity) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = prepareLocationMonitorPendingIntent();

        if (!isLocationMonitorAlarmSet()) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + locationMonitorOffset, locationMonitorInterval, pendingIntent);

            saveLocationMonitorAlarmWasSet(true);
            Toast.makeText(mainActivity, "Start listening for updates", Toast.LENGTH_SHORT).show();

        } else {
            alarmManager.cancel(pendingIntent);
            saveLocationMonitorAlarmWasSet(false);
            LocationMonitorService.clearLastArea(this);
            Toast.makeText(mainActivity, "Stop listening for updates", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isLocationMonitorAlarmSet() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(LOCATION_MONITOR_ALARM, false);
    }

    private PendingIntent prepareLocationMonitorPendingIntent() {
        Intent intent = new Intent(getApplicationContext(), LocationMonitorService.class);

        return PendingIntent.getService(this, LocationMonitorService.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void saveLocationMonitorAlarmWasSet(boolean isSet) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences
                .edit()
                .putBoolean(LOCATION_MONITOR_ALARM, isSet)
                .commit();
    }

    public AreasManager getAreasManager() {
        if (areasManager == null) {
            areasManager = new AreasManagerImpl(this, getNetworkManager());
        }

        return areasManager;
    }

    public TrackingManager getTrackingManager() {
        if (trackingManager == null) {
            trackingManager = new TrackingManagerImpl(this);
        }

        return trackingManager;
    }

    private NetworkManager getNetworkManager() {
        if (networkManager == null) {
            networkManager = new NetworkManagerImpl(this);
        }

        return networkManager;
    }
}
