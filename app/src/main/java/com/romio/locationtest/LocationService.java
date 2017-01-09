
package com.romio.locationtest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {
    public static final String START = "com.romio.locationtest.location.service.start";
    public static final String DATA = "com.romio.locationtest.location.service.data";
    public static final String STOP = "com.romio.locationtest.location.service.stop";

    private static final String TAG = LocationService.class.getSimpleName();
    private static final int MAX_NOTIFICATION_ID_NUMBER = 1000;
    private GoogleApiClient googleApiClient;
    private ArrayList<TargetArea> targets;
    private TargetArea currentArea;
    private static int notificationId = 0;

    enum Movement {
        ENTER_AREA("Enter area"), LEAVE_AREA("Leave area");

        private String name;

        Movement(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            switch (intent.getAction()) {
                case START: {
                    targets = intent.getParcelableArrayListExtra(DATA);

                    startLocationService();
                    buildApiClient();
                }
                break;

                case STOP: {
                    targets = null;
                    notifyServiceWasStopped();
                    shutdown();
                }
            }
        }

        return START_STICKY;
    }

    private void notifyServiceWasStopped() {
        Intent intent = new Intent();
        intent.setAction(LocationService.STOP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void startLocationService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putParcelableArrayListExtra(DATA, targets);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, LocationService.class);
        stopIntent.setAction(STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notification = new Notification.Builder(this)
                .setContentTitle("Location Monitor")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.ic_global_18dp)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_stop_18dp, getString(R.string.stop), stopPendingIntent);
        startForeground(101, notification.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void buildApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(connectionCallback)
                    .addOnConnectionFailedListener(onConnectionFailedListener)
                    .addApi(LocationServices.API)
                    .build();
        }

        googleApiClient.connect();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);

        } else {
            shutdown();
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Location updated: Latitude = " + location.getLatitude() + "   longitude = " + location.getLongitude());
            processLocationUpdate(location);
        }
    };

    private void processLocationUpdate(Location location) {
        if (targets == null) {
            Log.d(TAG, "No target areas specified");

        } else {
            TargetArea newTargetArea = getCurrentArea(location);
            String area = (newTargetArea != null) ? newTargetArea.getAreaName() : "null";
            Log.d(TAG, "Current area: " + area);

            if (currentArea == null && newTargetArea != null) {
                notifyUserChangePosition(newTargetArea, Movement.ENTER_AREA);
            }

            if (currentArea != null && newTargetArea == null) {
                notifyUserChangePosition(currentArea, Movement.LEAVE_AREA);
            }

            if (currentArea != null && newTargetArea != null && currentArea != newTargetArea) {
                notifyUserChangePosition(currentArea, newTargetArea);
            }

            currentArea = newTargetArea;
        }
    }

    private void notifyUserChangePosition(TargetArea currentArea, TargetArea newTargetArea) {
        String message = "Areas changed from " + currentArea.getAreaName() + " to " + newTargetArea.getAreaName();
        String title = "Areas changed";

        notifyUser(message, title);
    }

    private void notifyUserChangePosition(TargetArea area, @NonNull Movement enterArea) {
        String message = enterArea.getName() + " " + area.getAreaName();
        String title = "Area status changed";

        notifyUser(message, title);
    }

    private void notifyUser(String message, String title) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_add_location_18dp)
                        .setContentTitle(title)
                        .setContentText(message);

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putParcelableArrayListExtra(DATA, targets);

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

    private TargetArea getCurrentArea(Location location) {
        for (TargetArea targetArea : targets) {
            if (isInside(targetArea, location)) {
                return targetArea;
            }
        }

        return null;
    }

    private boolean isInside(TargetArea targetArea, Location location) {
        double distance = distance(
                targetArea.getAreaCenter().latitude,
                targetArea.getAreaCenter().longitude,
                location.getLatitude(),
                location.getLongitude());

        return distance <= targetArea.getRadius();
    }

    public static double distance(
            double lat1, double lng1, double lat2, double lng2) {
        int earthRadius = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double hipotenuse = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * hipotenuse * 1000;
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(getResources().getInteger(R.integer.time_interval));
        locationRequest.setFastestInterval(getResources().getInteger(R.integer.time_interval_fastest));
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        return locationRequest;
    }

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(LocationService.this, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
            LocationService.this.shutdown();
        }
    };

    private void shutdown() {
        stopListeningLocationUpdates();
        stopForeground(true);
        stopSelf();
    }

    private void stopListeningLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
        googleApiClient.disconnect();
    }

    private GoogleApiClient.ConnectionCallbacks connectionCallback = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            LocationService.this.startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Toast.makeText(LocationService.this, "Connection Suspended", Toast.LENGTH_SHORT).show();
        }
    };
}
