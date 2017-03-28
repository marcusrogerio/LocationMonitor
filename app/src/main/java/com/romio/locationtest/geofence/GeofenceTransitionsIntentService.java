package com.romio.locationtest.geofence;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.tracking.LocationManager;
import com.romio.locationtest.utils.NotificationUtils;

/**
 * Created by roman on 3/23/17
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String NAME = GeofenceTransitionsIntentService.class.getName();

    public GeofenceTransitionsIntentService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LocationMonitorApp app = (LocationMonitorApp) getApplication();
        LocationManager locationManager = app.getLocationManager();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            locationManager.stopLocationMonitorService();
            NotificationUtils.hidePermanentNotification(app);

            if (GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE == geofencingEvent.getErrorCode()) {
                NotificationUtils.notifyUser(getApplicationContext(), "Location is disabled. App will not work");

            } else {
                NotificationUtils.notifyUser(getApplicationContext(), "Error code: " + geofencingEvent.getErrorCode());
            }

        } else {
            switch (geofencingEvent.getGeofenceTransition()) {
                case Geofence.GEOFENCE_TRANSITION_DWELL: {
                    String areaName = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();
//                    NotificationUtils.notifyUser(getApplicationContext(), "Entered " + areaName);
                    NotificationUtils.showPermanentNotification(getApplicationContext(), "Entered " + areaName);

                    locationManager.startLocationMonitorService();
                }
                break;
                case Geofence.GEOFENCE_TRANSITION_EXIT: {
                    String areaName = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();
//                    NotificationUtils.notifyUser(getApplicationContext(), "Exited " + areaName);
                    NotificationUtils.hidePermanentNotification(app);

                    locationManager.stopLocationMonitorService();
                }
                break;
            }
        }
    }
}