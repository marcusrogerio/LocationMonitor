package com.romio.locationtest.geofence;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.ui.MainActivity;
import com.romio.locationtest.ui.SplashActivity;

/**
 * Created by roman on 3/23/17
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String NAME = GeofenceTransitionsIntentService.class.getName();
    private static final int MAX_NOTIFICATION_ID_NUMBER = 1000;
    private static int notificationId = 0;

    public GeofenceTransitionsIntentService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LocationMonitorApp app = (LocationMonitorApp) getApplication();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            notifyUser("Error code: " + geofencingEvent.getErrorCode(), "Location Monitor Error");

        } else {
            switch (geofencingEvent.getGeofenceTransition()) {
                case Geofence.GEOFENCE_TRANSITION_DWELL: {
                    String areaName = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();
                    notifyUser("Entered " + areaName, "Location Monitor");

                    app.startLocationMonitorService();
                }
                break;
                case Geofence.GEOFENCE_TRANSITION_EXIT: {
                    String areaName = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();
                    notifyUser("Exited " + areaName, "Location Monitor");

                    app.stopLocationMonitorService();
                }
                break;
            }
        }
    }

    private void notifyUser(String message, String title) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.btn_dialog)
                        .setContentTitle(title)
                        .setLights(Color.BLUE, 500, 500)
                        .setSound(alarmSound)
                        .setContentText(message);

        Intent resultIntent = new Intent(this, SplashActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());

        if (notificationId == MAX_NOTIFICATION_ID_NUMBER) {
            notificationId = 0;
        } else {
            notificationId++;
        }
    }
}