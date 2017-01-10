package com.romio.locationtest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by roman on 1/10/17.
 */

public class LocationIntentService extends Service {

    private static final String TAG = LocationIntentService.class.getSimpleName();
    private static final String SERVICE_NAME = LocationIntentService.class.getName();
    public static final String DATA = "com.romio.locationtest.location.service.data";

    private static final int MAX_NOTIFICATION_ID_NUMBER = 1000;
    private static final int MAX_NUMBER_OF_FAILED_LOCATION_UPDATES = 5;
    private static int notificationId = 0;
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private GoogleApiClient googleApiClient;
    private ArrayList<TargetArea> targets;
    private TargetArea currentArea;
    private boolean mRedelivery;
    private int startId;
    private int numberOfFailedUpdates;
    private boolean isRunning = false;

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent)msg.obj);
            startId = msg.arg1;
        }
    }

    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("IntentService[" + SERVICE_NAME + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    private void onHandleIntent(Intent intent) {
        targets = intent.getParcelableArrayListExtra(DATA);
        buildApiClient();
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

            numberOfFailedUpdates = 0;
        } else {
            shutdown();
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.d(TAG, "Location updated: Latitude = " + location.getLatitude() + "   longitude = " + location.getLongitude());
                Toast.makeText(LocationIntentService.this, "Location updated", Toast.LENGTH_SHORT).show();
                processLocationUpdate(location);

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

    private void processLocationUpdate(Location location) {
        if (targets == null) {
            Log.d(TAG, "No target areas specified");

        } else {
            TargetArea newTargetArea = getCurrentArea(location);
            String area = (newTargetArea != null) ? newTargetArea.getAreaName() : "null";
            Log.d(TAG, "Current area: " + area);

            if (currentArea == null && newTargetArea != null) {
                notifyUserChangePosition(newTargetArea, LocationIntentService.Movement.ENTER_AREA);
            }

            if (currentArea != null && newTargetArea == null) {
                notifyUserChangePosition(currentArea, LocationIntentService.Movement.LEAVE_AREA);
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

    private void notifyUserChangePosition(TargetArea area, @NonNull LocationIntentService.Movement enterArea) {
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
            Toast.makeText(LocationIntentService.this, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
            LocationIntentService.this.shutdown();
        }
    };

    private void stopListeningLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
        googleApiClient.disconnect();
    }

    private GoogleApiClient.ConnectionCallbacks connectionCallback = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@org.jetbrains.annotations.Nullable Bundle bundle) {
            LocationIntentService.this.startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Toast.makeText(LocationIntentService.this, "Connection Suspended", Toast.LENGTH_SHORT).show();
        }
    };

    private void shutdown() {
        stopListeningLocationUpdates();
        stopSelf(startId);
    }
}
