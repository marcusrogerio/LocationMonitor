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
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.common.api.GoogleApiClient;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.romio.locationtest.data.manager.AreasManager;
import com.romio.locationtest.data.manager.AreasManagerImpl;
import com.romio.locationtest.data.manager.TrackingManager;
import com.romio.locationtest.data.manager.TrackingManagerImpl;
import com.romio.locationtest.data.db.DBHelper;
import com.romio.locationtest.data.db.DBManager;
import com.romio.locationtest.data.db.DataBaseHelper;
import com.romio.locationtest.geofence.GeofenceManager;
import com.romio.locationtest.service.LocationMonitorService;
import com.romio.locationtest.ui.MainActivity;
import com.romio.locationtest.utils.NetworkManager;
import com.romio.locationtest.utils.NetworkManagerImpl;

import io.fabric.sdk.android.Fabric;

/**
 * Created by roman on 1/9/17
 */

public class LocationMonitorApp extends Application implements DBHelper {

    public static final String TAG = LocationMonitorApp.class.getSimpleName();
    private static final String LOCATION_MONITOR_ALARM = "com.romio.locationtest.alarm.location_monitor";
    private static final String LOCATION_DATA_UPLOAD = "com.romio.locationtest.location_data_upload";
    private static final String JOB_TAG = LocationMonitorApp.class.getSimpleName();

    private AreasManager areasManager;
    private TrackingManager trackingManager;
    private NetworkManager networkManager;
    private DataBaseHelper databaseHelper;

    private int locationMonitorOffset = 3000;
    private int locationMonitorInterval = 120000;
    private GoogleApiClient googleApiClient;
    private GeofenceManager geofenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        locationMonitorOffset = getResources().getInteger(R.integer.location_monitor_time_offset);
        locationMonitorInterval = getResources().getInteger(R.integer.location_monitor_time_interval);

        initUploadDataScheduler();
    }

    public AreasManager getAreasManager() {
        if (areasManager == null) {
            areasManager = new AreasManagerImpl(this, getNetworkManager());
        }

        return areasManager;
    }

    public TrackingManager getTrackingManager() {
        if (trackingManager == null) {
            trackingManager = new TrackingManagerImpl(this, networkManager, this);
        }

        return trackingManager;
    }

    private NetworkManager getNetworkManager() {
        if (networkManager == null) {
            networkManager = new NetworkManagerImpl(this);
        }

        return networkManager;
    }

    public DBHelper getDBHelper() {
        return this;
    }

    @Override
    public DBManager getDbManager() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DataBaseHelper.class);
        }

        return databaseHelper;
    }

    @Override
    public void release() {
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }


    /**
     * location service section
     */

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

    /**
     * data upload section
     */
    public void initUploadDataScheduler() {
        if (!isDataUploadServiceSet()) {
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
            Job myJob = dispatcher.newJobBuilder()
                    .setService(UploadTrackingDataJobService.class)
                    .setTag(JOB_TAG)
                    .setRecurring(true)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.executionWindow(60, 180))
                    .setReplaceCurrent(false)
                    .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .build();

            dispatcher.mustSchedule(myJob);

            setDataUploadServiceState(true);
        }
    }

    private boolean isDataUploadServiceSet() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean(LOCATION_DATA_UPLOAD, false);
    }

    private void setDataUploadServiceState(boolean isSet) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putBoolean(LOCATION_DATA_UPLOAD, isSet).apply();
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public GeofenceManager getGeofenceManager() {
        if (geofenceManager == null) {
            geofenceManager = new GeofenceManager(this);
        }
        
        return geofenceManager;
    }

    public void setGeofenceManager(GeofenceManager geofenceManager) {
        this.geofenceManager = geofenceManager;
    }
}
