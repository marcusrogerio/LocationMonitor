package com.romio.locationtest.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.R;
import com.romio.locationtest.data.AreaDto;
import com.romio.locationtest.tracking.LocationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 3/26/17
 */

public class GeofenceManagerImpl implements GeofenceManager {

    public static final int PENDING_INTENT_REQUEST_CODE = 177;
    private static final String GEOFENCING_RUNNING = "com.romio.locationtest.geofencing";
    private static final String TAG = GeofenceManager.class.getSimpleName();

    private LocationMonitorApp app;
    private List<Geofence> geofenceList = new ArrayList<>();
    private LocationManager locationManager;
    private int loiteringDelayInMilliseconds;
    private int notificationResponsivenessInMilliseconds;

    public GeofenceManagerImpl(LocationMonitorApp app, LocationManager locationManager) {
        this.app = app;
        this.locationManager = locationManager;

        loiteringDelayInMilliseconds = app.getResources().getInteger(R.integer.loitering_delay);
        notificationResponsivenessInMilliseconds = app.getResources().getInteger(R.integer.notification_responsiveness);
    }

    @Override
    public boolean isGeofencing() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
        return sharedPreferences.getBoolean(GEOFENCING_RUNNING, false);
    }

    @Override
    public void startGeofencingAfterGeofenceAreasChanged() {
        // TODO: 3/26/17 analyze areas (old, new etc)
        if (!isGeofencing()) {
            startGeofencing();
        }
    }

    @Override
    public void startGeofencingAfterReboot() {
        locationManager.stopLocationMonitorService();
        startGeofencing();
    }

    @Override
    public void startGeofencingAfterLocationSettingsChanged() {
        startGeofencing();
    }

    @Override
    public void stopGeofencing() {
        LocationServices.GeofencingApi.removeGeofences(
                app.getGoogleApiClient(),
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    Log.i(TAG, "Geofence stopped successfully");

                } else {
                    Log.i(TAG, "Failed to stop Geofence " + status.getStatusMessage());
                }
            }
        });

        setGeofensingStatus(false);
    }

    private GeofencingRequest getGeofencingRequest() {
        List<AreaDto> areas = app.getAreasManager().getGeofenceAreasFromDB();
        if (areas != null && !areas.isEmpty()) {
            for (AreaDto areaDto : areas) {
                if (areaDto.isEnabled()) {
                    addGeofence(areaDto.getLatitude(), areaDto.getLongitude(), areaDto.getRadius(), areaDto.getAreaName());
                }
            }
        }

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT | GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofenceList);

        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(app, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(app, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void startGeofencing() {
        if (ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (!hasGeofenceAreas()) {
            return;
        }

        LocationServices.GeofencingApi.addGeofences(
                app.getGoogleApiClient(),
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(getResultCallback);

        setGeofensingStatus(true);
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

    private void setGeofensingStatus(boolean isRunning) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
        sharedPreferences
                .edit()
                .putBoolean(GEOFENCING_RUNNING, isRunning)
                .commit();
    }

    private boolean hasGeofenceAreas() {
        List<AreaDto> areas = app.getAreasManager().getGeofenceAreasFromDB();
        if (areas != null && !areas.isEmpty()) {
            return true;
        }

        return false;
    }

    private ResultCallback<Status> getResultCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(@NonNull Status status) {
            if (!status.isSuccess()) {
                throw new RuntimeException(status.getStatusMessage());
            }

        }
    };
}
