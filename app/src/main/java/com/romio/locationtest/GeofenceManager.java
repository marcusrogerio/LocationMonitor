package com.romio.locationtest;

import android.app.PendingIntent;

import com.google.android.gms.location.GeofencingRequest;

import java.util.ArrayList;

/**
 * Created by roman on 2/4/17
 */

public interface GeofenceManager {
    void addTarget(TargetArea targetArea);

    void clearAll();

    boolean containsGeofences();

    GeofencingRequest getGeofencingRequest();

    PendingIntent getGeofencePendingIntent();

    ArrayList<TargetArea> readTargets();

}
