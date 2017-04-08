package com.romio.locationtest.tracking;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.romio.locationtest.R;
import com.romio.locationtest.service.LocationMonitorService;

/**
 * Created by roman on 3/26/17.
 */

public class LocationManagerImpl implements LocationManager {

    private static final String LOCATION_MONITOR_ALARM = "com.romio.locationtest.alarm.location_monitor";
    private Context context;
    private int locationMonitorOffset = 3000;
    private int locationMonitorInterval = 120000;

    public LocationManagerImpl(Context context) {
        this.context = context;

        locationMonitorOffset = context.getResources().getInteger(R.integer.start_location_monitor_time_offset);
        locationMonitorInterval = context.getResources().getInteger(R.integer.location_monitor_time_interval);
    }

    @Override
    public void startLocationMonitorService() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = prepareLocationMonitorPendingIntent();

        if (!isLocationMonitorAlarmSet()) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + locationMonitorOffset, locationMonitorInterval, pendingIntent);
            setLocationMonitorAlarmStatus(true);
        }
    }

    @Override
    public void stopLocationMonitorService() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = prepareLocationMonitorPendingIntent();

        alarmManager.cancel(pendingIntent);
        setLocationMonitorAlarmStatus(false);
    }

    @Override
    public boolean isLocationMonitorAlarmSet() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(LOCATION_MONITOR_ALARM, false);
    }

    private PendingIntent prepareLocationMonitorPendingIntent() {
        Intent intent = new Intent(context, LocationMonitorService.class);

        return PendingIntent.getService(context, LocationMonitorService.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setLocationMonitorAlarmStatus(boolean isSet) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putBoolean(LOCATION_MONITOR_ALARM, isSet)
                .commit();
    }
}
