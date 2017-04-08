package com.romio.locationtest.geofence;

/**
 * Created by roman on 3/23/17
 */

public interface GeofenceManager {
    boolean isGeofencing();

    void startGeofencingAfterReboot();

    void startGeofencingAfterLocationSettingsChanged();

    void stopGeofencing();

    void restart();
}
