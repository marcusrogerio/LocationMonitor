package com.romio.locationtest.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.data.ZoneType;
import com.romio.locationtest.data.repository.TrackingManager;
import com.romio.locationtest.ui.MainActivity;
import com.romio.locationtest.R;
import com.romio.locationtest.utils.WakeLocker;
import com.romio.locationtest.data.repository.AreasManager;
import com.romio.locationtest.data.AreaDto;
import com.romio.locationtest.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by roman on 1/10/17
 */

public class LocationMonitorService extends Service {

    public static final int REQUEST_CODE = 103;

    private static final String TAG = LocationMonitorService.class.getSimpleName();
    private static final String SERVICE_NAME = LocationMonitorService.class.getName();

    private static final String CURRENT_AREA_LATITUDE = "com.romio.locationtest.location.current.latItude";
    private static final String CURRENT_AREA_ID = "com.romio.locationtest.location.current.id";
    private static final String CURRENT_AREA_LONGITUDE = "com.romio.locationtest.location.current.longitude";
    private static final String CURRENT_AREA_RADIUS = "com.romio.locationtest.location.current.radius";
    private static final String CURRENT_AREA_NAME = "com.romio.locationtest.location.current.name";
    private static final String TIME_INSIDE_AREA_UPDATE = "com.romio.locationtest.location.area.inside.time";

    private static final int MAX_NOTIFICATION_ID_NUMBER = 1000;
    private static int notificationId = 0;
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private List<AreaDto> targets = new ArrayList<>();
    private TrackingManager trackingManager;
    private boolean mRedelivery;
    private int startId;

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
            onHandleIntent((Intent) msg.obj);
            startId = msg.arg1;
        }
    }

    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WakeLocker.acquire(this);
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
        WakeLocker.acquire(this);
        onStart(intent, startId);
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    public static void clearLastArea(Context context) {
        saveCurrentArea(null, context);
    }

    private void onHandleIntent(Intent intent) {
        AreasManager areasManager = ((LocationMonitorApp) getApplication()).getAreasManager();
        trackingManager = ((LocationMonitorApp) getApplication()).getTrackingManager();

        setTargetAreas(areasManager);
        getCurrentLocation();
    }

    private void setTargetAreas(AreasManager areasManager) {
        List<AreaDto> allAreas = areasManager.getCheckpointsFromDB();
        if (allAreas != null && !allAreas.isEmpty()) {
            for (AreaDto areaDto : allAreas) {
                if (areaDto.isEnabled()) {
                    targets.add(areaDto);
                }
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                LocationUtils.isLocationEnabled(this)) {

            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            String provider = setProvider(locationManager);

            if (provider != null) {
                locationManager.requestSingleUpdate(provider, locationListener, null);

            } else {
                notifyUser("No location providers available", "Location Monitor");
                shutdown();
            }

        } else {
            Log.w(TAG, "Location permission wasn't granted or location wasn't enabled");
            shutdown();
        }
    }

    private String setProvider(LocationManager locationManager) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;

        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;

        } else {
            return null;
        }
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                processLocationUpdate(location);
            } else {
                notifyUser("Location is null", "LocationMonitor");
            }

            shutdown();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            notifyUser(provider + " is Disabled", "LocationMonitor");
        }
    };

    private void processLocationUpdate(Location location) {
        if (targets == null) {
            Log.d(TAG, "No target areas specified");

        } else {
            AreaDto lastArea = retrieveOldArea();
            AreaDto newTargetArea = getCurrentArea(location);

            String area = (newTargetArea != null) ? newTargetArea.getAreaName() : "null";
            Log.d(TAG, "Current area: " + area);

            if (lastArea == null && newTargetArea != null) {
                trackingManager.enterArea(newTargetArea, location);
            }

            if (lastArea != null && newTargetArea == null) {
                trackingManager.leaveArea(lastArea, location);
            }

            if (lastArea != null && newTargetArea != null && !lastArea.equals(newTargetArea)) {
                trackingManager.changeArea(lastArea, newTargetArea, location);
            }

            saveCurrentArea(newTargetArea, this);

            if (newTargetArea != null) {
                notifyUserIsInArea(newTargetArea, location);
            }
        }
    }

    private void notifyUserIsInArea(AreaDto targetArea, Location location) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long currentMoment = new Date().getTime();

        if (preferences.contains(TIME_INSIDE_AREA_UPDATE)) {
            long lastUpdate = preferences.getLong(TIME_INSIDE_AREA_UPDATE, currentMoment);
            long areaMonitorInterval = getResources().getInteger(R.integer.area_monitor_time_interval) * 1000;

            if (currentMoment - lastUpdate >= areaMonitorInterval) {
                trackingManager.wanderInArea(targetArea, location);
                preferences.edit().putLong(TIME_INSIDE_AREA_UPDATE, currentMoment).commit();
            }

        } else {
            trackingManager.wanderInArea(targetArea, location);
            preferences.edit().putLong(TIME_INSIDE_AREA_UPDATE, currentMoment).commit();
        }
    }

    private AreaDto retrieveOldArea() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.contains(CURRENT_AREA_NAME)) {
            return null;
        }

        double latitude = sharedPreferences.getFloat(CURRENT_AREA_LATITUDE, -1);
        double longitude = sharedPreferences.getFloat(CURRENT_AREA_LONGITUDE, -1);
        int radius = sharedPreferences.getInt(CURRENT_AREA_RADIUS, -1);
        String name = sharedPreferences.getString(CURRENT_AREA_NAME, "");
        String id = sharedPreferences.getString(CURRENT_AREA_ID, "");

        return new AreaDto(id, name, latitude, longitude, radius, ZoneType.CHECKPOINT);
    }

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

    private static void saveCurrentArea(AreaDto newTargetArea, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (newTargetArea == null) {
            sharedPreferences
                    .edit()
                    .remove(CURRENT_AREA_LATITUDE)
                    .remove(CURRENT_AREA_LONGITUDE)
                    .remove(CURRENT_AREA_RADIUS)
                    .remove(CURRENT_AREA_NAME)
                    .remove(CURRENT_AREA_ID)
                    .commit();

        } else {
            sharedPreferences
                    .edit()
                    .putFloat(CURRENT_AREA_LATITUDE, (float) newTargetArea.getLatitude())
                    .putFloat(CURRENT_AREA_LONGITUDE, (float) newTargetArea.getLongitude())
                    .putInt(CURRENT_AREA_RADIUS, newTargetArea.getRadius())
                    .putString(CURRENT_AREA_NAME, newTargetArea.getAreaName())
                    .putString(CURRENT_AREA_ID, newTargetArea.getId())
                    .commit();
        }
    }

    private AreaDto getCurrentArea(Location location) {
        for (AreaDto areaDto : targets) {
            if (LocationUtils.isInside(areaDto, location)) {
                return areaDto;
            }
        }

        return null;
    }

    private void shutdown() {
        targets = new ArrayList<>();
        LocationMonitorApp app = (LocationMonitorApp) getApplication();
        app.getDBHelper().release();

        stopSelf(startId);
        WakeLocker.release();
    }
}
