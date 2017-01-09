
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

public class LocationService extends Service {
    public static final String START = "com.romio.locationtest.location.service.start";
    public static final String STOP = "com.romio.locationtest.location.service.stop";
    public static final String LATITUDE = "com.romio.locationtest.location.latitude";
    public static final String LONGITUDE = "com.romio.locationtest.location.longitude";

    private static final String TAG = LocationService.class.getSimpleName();
    private GoogleApiClient googleApiClient;

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            switch (intent.getAction()) {
                case START : {
                    startLocationService();
                    buildApiClient();
                }  break;

                case STOP : {
                    shutdown();
                }
            }
        }

        return START_STICKY;
    }

    private void startLocationService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(LocationService.class.getSimpleName());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent stopIntent = new Intent(this, LocationService.class);
        stopIntent.setAction(STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

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
            showNotification(location);
        }
    };

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        return locationRequest;
    }

    private void showNotification(Location location) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_add_location_18dp)
                        .setContentTitle("Location changed")
                        .setContentText("Latitude: " + location.getLatitude() + " Longitude:" + location.getLongitude());

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(LATITUDE, location.getLatitude());
        resultIntent.putExtra(LONGITUDE, location.getLongitude());

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(LocationService.this, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
            LocationService.this.shutdown();
        }
    };

    private void shutdown() {
        stopForeground(true);
        stopSelf();
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
