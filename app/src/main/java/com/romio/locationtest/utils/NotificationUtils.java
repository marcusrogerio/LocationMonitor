package com.romio.locationtest.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.romio.locationtest.R;
import com.romio.locationtest.ui.SplashActivity;

/**
 * Created by roman on 3/28/17
 */

public class NotificationUtils {

    private static final String NOTIFICATION_ID_KEY = "com.romio.locationtest.utils.NotificationUtils.NOTIFICATION_ID_KEY";
    private static final int MAX_NOTIFICATION_ID_NUMBER = 1000;

    public synchronized static void notifyUser(Context context, String message) {
        String appName = context.getString(R.string.app_name);
        notifyUser(context, message, appName);
    }

    public synchronized static void notifyUser(Context context, String message, String title) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_add_location_18dp)
                        .setContentTitle(title)
                        .setLights(Color.BLUE, 500, 500)
                        .setSound(alarmSound)
                        .setContentText(message);

        buildAndNotify(context, builder);
    }

    public synchronized static void showPermanentNotification(Context context, String message) {
        String appName = context.getString(R.string.app_name);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_global_18dp)
                        .setContentTitle(appName)
                        .setLights(Color.BLUE, 500, 500)
                        .setSound(alarmSound)
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setContentText(message);

        buildAndNotify(context, builder);
    }

    public synchronized static void hidePermanentNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private static void buildAndNotify(Context context, NotificationCompat.Builder builder) {
        Intent resultIntent = getResultIntent(context);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = generateNotificationId(context);
        notificationManager.notify(notificationId, builder.build());
    }

    private static Intent getResultIntent(Context context) {
        Intent resultIntent = new Intent(context, SplashActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return resultIntent;
    }

    private static int generateNotificationId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(NotificationUtils.class.getSimpleName(), Context.MODE_PRIVATE);
        int notificationId = preferences.getInt(NOTIFICATION_ID_KEY, 0);

        if (notificationId == MAX_NOTIFICATION_ID_NUMBER) {
            notificationId = 0;
        } else {
            notificationId++;
        }

        preferences.edit().putInt(NOTIFICATION_ID_KEY, notificationId).commit();

        return notificationId;
    }
}
