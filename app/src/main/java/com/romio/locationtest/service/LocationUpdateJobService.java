package com.romio.locationtest.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.MainActivity;
import com.romio.locationtest.R;
import com.romio.locationtest.TargetArea;
import com.romio.locationtest.Utils;

import java.util.ArrayList;

/**
 * Created by roman on 2/3/17
 */

public class LocationUpdateJobService extends JobService {

    private static final String TAG = LocationUpdateJobService.class.getSimpleName();
    private static final int MAX_NOTIFICATION_ID_NUMBER = 1000;
    private static final int MAX_NUMBER_OF_FAILED_LOCATION_UPDATES = 5;
    private static int notificationId = 0;
    private GoogleApiClient googleApiClient;
    private ArrayList<TargetArea> targets;
    private int numberOfFailedUpdates;

    @Override
    public boolean onStartJob(JobParameters job) {
        targets = ((LocationMonitorApp)getApplication()).readTargets();
        buildApiClient();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            notifyUser("LocationUpdateJobService ", "LocationListener");
            if (location != null) {
//                processLocationUpdate(location);

                shutdown();

            } else {
                numberOfFailedUpdates++;
            }

            if (numberOfFailedUpdates == MAX_NUMBER_OF_FAILED_LOCATION_UPDATES) {
                Log.e(TAG, "Can't obtain location");
                shutdown();
            }
        }
    };

    private void notifyUser(String message, String title) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_add_location_18dp)
                        .setContentTitle(title)
                        .setLights(Color.BLUE, 500, 500)
                        .setSound(alarmSound)
                        .setContentText(message);

        Intent resultIntent = new Intent(this, MainActivity.class);
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

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                Utils.isLocationEnabled(this)) {

            LocationRequest locationRequest = createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);

            numberOfFailedUpdates = 0;

        } else {
            Log.w(TAG, "Location permission wasn't granted or location wasn't enabled");
            shutdown();
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(getResources().getInteger(R.integer.time_interval));
        locationRequest.setFastestInterval(getResources().getInteger(R.integer.time_interval_fastest));
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        return locationRequest;
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

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(LocationUpdateJobService.this, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
            LocationUpdateJobService.this.shutdown();
        }
    };

    private void stopListeningLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
        googleApiClient.disconnect();
    }

    private GoogleApiClient.ConnectionCallbacks connectionCallback = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@org.jetbrains.annotations.Nullable Bundle bundle) {
            LocationUpdateJobService.this.startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Toast.makeText(LocationUpdateJobService.this, "Connection Suspended", Toast.LENGTH_SHORT).show();
        }
    };

    private void shutdown() {
        targets = new ArrayList<>();
        ((LocationMonitorApp)getApplication()).releaseDBManager();

        stopListeningLocationUpdates();
        stopSelf();
    }
}
