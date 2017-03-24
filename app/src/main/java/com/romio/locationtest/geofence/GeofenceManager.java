package com.romio.locationtest.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 3/23/17
 */

public class GeofenceManager {

    public static final int PENDING_INTENT_REQUEST_CODE = 177;
    private static final String GEOFENCING_RUNNING = "com.romio.locationtest.geofencing";
    private static final String TAG = GeofenceManager.class.getSimpleName();

    public static final double LATITUDE = 49.839837;
    public static final double LONGITUDE = 24.028919;
    public static final int RADIUS = 650;

    private LocationMonitorApp app;
    private List<Geofence> geofenceList = new ArrayList<>();
    private int loiteringDelayInMilliseconds;
    private int notificationResponsivenessInMilliseconds;

    public GeofenceManager(LocationMonitorApp app) {
        this.app = app;

        loiteringDelayInMilliseconds = app.getResources().getInteger(R.integer.loitering_delay);
        notificationResponsivenessInMilliseconds = app.getResources().getInteger(R.integer.notification_responsiveness);
    }

    public void startGeofencingIfNotStarted(ResolvingResultCallbacks<Status> statusResolvingResultCallback) {
        if (!isGeofencing()) {
            if (ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LocationServices.GeofencingApi.addGeofences(
                    app.getGoogleApiClient(),
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(statusResolvingResultCallback);

            setGeofensingStatus(true);
        }
    }

    public void stopGeofencing(ResolvingResultCallbacks<Status> statusResolvingResultCallback) {
        LocationServices.GeofencingApi.removeGeofences(
                app.getGoogleApiClient(),
                app.getGeofenceManager().getGeofencePendingIntent()
        ).setResultCallback(statusResolvingResultCallback);

        setGeofensingStatus(false);
    }

    public GeofencingRequest getGeofencingRequest() {
        addGeofence(LATITUDE, LONGITUDE, RADIUS, "GeofenceArea");

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT | GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    public PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(app, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(app, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void addGeofence(double latitude, double longitude, float radius, String areaId) {
        geofenceList.add(new Geofence.Builder()
                .setRequestId(areaId)
                .setNotificationResponsiveness(notificationResponsivenessInMilliseconds)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(loiteringDelayInMilliseconds)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build());
    }

    public boolean isGeofencing() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
        return sharedPreferences.getBoolean(GEOFENCING_RUNNING, false);
    }

    private void setGeofensingStatus(boolean isRunning) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
        sharedPreferences
                .edit()
                .putBoolean(GEOFENCING_RUNNING, isRunning)
                .commit();
    }
}
