package com.romio.locationtest.geofence;

/**
 * Created by roman on 3/23/17
 */

public interface GeofenceManager {
    boolean isGeofencing();

    void startGeofencingAfterGeofenceAreasChanged();

    void startGeofencingAfterReboot();

    void startGeofencingAfterLocationSettingsChanged();

    void stopGeofencing();
}
