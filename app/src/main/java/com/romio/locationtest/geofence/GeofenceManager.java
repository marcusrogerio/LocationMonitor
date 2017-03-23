package com.romio.locationtest.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.romio.locationtest.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 3/23/17
 */

public class GeofenceManager {

    public static final int PENDING_INTENT_REQUEST_CODE = 177;
    private static final String TAG = GeofenceManager.class.getSimpleName();
    private int loiteringDelayInMilliseconds;
    private int notificationResponsivenessInMilliseconds;
    private List<Geofence> geofenceList = new ArrayList<>();

    private Context context;

    public static final double LATITUDE = 49.830105;
    public static final double LONGITUDE = 24.006869;
    public static final int RADIUS = 800;

    public GeofenceManager(Context context) {
        this.context = context;

        loiteringDelayInMilliseconds = context.getResources().getInteger(R.integer.loitering_delay);
        notificationResponsivenessInMilliseconds = context.getResources().getInteger(R.integer.notification_responsiveness);
    }

    public GeofencingRequest getGeofencingRequest() {
        addGeofence(LATITUDE, LONGITUDE, RADIUS, "GeofenceArea");

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT | GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    public PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(context, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
}
