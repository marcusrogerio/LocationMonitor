package com.romio.locationtest.data.repository;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.romio.locationtest.R;
import com.romio.locationtest.data.AreaDto;
import com.romio.locationtest.ui.MainActivity;

/**
 * Created by roman on 3/24/17
 */

public class MockTrackingManager implements TrackingManager {

    private static final int MAX_NOTIFICATION_ID_NUMBER = 1000;
    private static int notificationId = 0;
    private Context context;

    public MockTrackingManager(Context context) {
        this.context = context;
    }

    @Override
    public void enterArea(AreaDto newTargetArea, Location location) {
        String message = "Entered " + newTargetArea.getAreaName();
        String title = "Area status changed";

        notifyUser(message, title);
    }

    @Override
    public void leaveArea(AreaDto lastArea, Location location) {
        String message = "Leaved " + lastArea.getAreaName();
        String title = "Area status changed";

        notifyUser(message, title);
    }

    @Override
    public void changeArea(AreaDto lastArea, AreaDto newTargetArea, Location location) {
        String message = "Areas changed from " + lastArea.getAreaName() + " to " + newTargetArea.getAreaName();
        String title = "Areas changed";

        notifyUser(message, title);
    }

    @Override
    public void wanderInArea(AreaDto targetArea, Location location) {
        String title = "Presence in Area";
        String message = targetArea.getAreaName() + ".\n Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude();

        notifyUser(message, title);
    }

    private void notifyUser(String message, String title) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_add_location_18dp)
                        .setContentTitle(title)
                        .setLights(Color.BLUE, 500, 500)
                        .setSound(alarmSound)
                        .setContentText(message);

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());

        if (notificationId == MAX_NOTIFICATION_ID_NUMBER) {
            notificationId = 0;
        } else {
            notificationId++;
        }
    }
}
