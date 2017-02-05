package com.romio.locationtest.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.romio.locationtest.R;
import com.romio.locationtest.TargetArea;
import com.romio.locationtest.data.DBManager;
import com.romio.locationtest.data.TargetAreaDto;
import com.romio.locationtest.data.TargetAreaMapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 2/4/17
 */

public class GeofenceManagerImpl implements GeofenceManager {

    public static final int PENDING_INTENT_REQUEST_CODE = 177;
    private static final String TAG = GeofenceManagerImpl.class.getSimpleName();
    private int loiteringDelayInMilliseconds;
    private int notificationResponsivenessInMilliseconds;
    private List<Geofence> geofenceList = new ArrayList<>();
    private DBManager dbManager;
    private Context context;

    public GeofenceManagerImpl(Context context, DBManager dbManager) {
        this.context = context;
        this.dbManager = dbManager;

        loiteringDelayInMilliseconds = context.getResources().getInteger(R.integer.loitering_delay);
        notificationResponsivenessInMilliseconds = context.getResources().getInteger(R.integer.notification_responsiveness);
    }

    @Override
    public GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT | GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    @Override
    public PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(context, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public boolean containsGeofences() {
        return !geofenceList.isEmpty();
    }

    @Override
    public ArrayList<TargetArea> readTargets() {
        try {
            List<TargetAreaDto> targetAreaDtoList = dbManager.getAreaDao().queryForAll();
            addAllTargetsToGeofenceList(targetAreaDtoList);

            return TargetAreaMapper.mapFromDto(targetAreaDtoList);

        } catch (SQLException e) {
            Log.e(TAG, "Error reading targets from DB", e);
            throw new RuntimeException(e);
        }
    }

    private void addAllTargetsToGeofenceList(List<TargetAreaDto> targetAreaDtoList) {
        if (targetAreaDtoList != null && !targetAreaDtoList.isEmpty()) {
            for (TargetAreaDto areaDto : targetAreaDtoList) {
                addTarget(areaDto.getLatitude(), areaDto.getLongitude(), areaDto.getRadius(), areaDto.getAreaName());
            }
        }
    }

    @Override
    public void addTarget(TargetArea targetArea) {
        TargetAreaDto targetAreaDto = TargetAreaMapper.map(targetArea);
        try {
            dbManager.getAreaDao().createOrUpdate(targetAreaDto);

            addTarget(
                    targetArea.getAreaCenter().latitude,
                    targetArea.getAreaCenter().longitude,
                    (float) targetArea.getRadius(),
                    targetArea.getAreaName());

        } catch (SQLException e) {
            Log.e(TAG, "Error adding area to DB", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearAll() {
        geofenceList.clear();
        dbManager.clearAll();
    }

    private void addTarget(double latitude, double longitude, float radius, String areaId) {
        geofenceList.add(new Geofence.Builder()
                .setRequestId(areaId)
                .setNotificationResponsiveness(notificationResponsivenessInMilliseconds)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(loiteringDelayInMilliseconds)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build());
    }
}
