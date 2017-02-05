package com.romio.locationtest.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.R;
import com.romio.locationtest.TargetArea;
import com.romio.locationtest.Utils;
import com.romio.locationtest.geofence.GeofenceManager;

import java.util.ArrayList;

import butterknife.BindColor;
import butterknife.BindInt;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SERVICE_IS_RUNNING = "com.romio.locationtest.service.is.running";

    private static final int GEOFENCE_REQUEST_CODE = 106;
    private static final int PERMISSION_REQUEST_CODE = 107;
    private static final int REQUEST_ENABLE_LOCATION = 102;
    private static int counter = 0;

    private LocationMonitorApp app;
    private GeofenceManager geofenceManager;
    private SupportMapFragment mapFragment;
    private ProgressBar progressBar;
    private MenuItem itemPlayStop;
    private GoogleMap map;

    private ArrayList<TargetArea> targets = new ArrayList<>();

    private boolean canStartService = false;

    private int zoom;
    private int radius;
    private int areaFillColor;
    private int boundColor;

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        itemPlayStop = menu.findItem(R.id.item_launch);
        updateMenuItemState();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_launch: {
                if (canStartService) {
                    toggleLocationService();
                } else {
                    Toast.makeText(MainActivity.this, "Can't start service yet", Toast.LENGTH_SHORT).show();
                }

                updateMenuItemState();
            }
            return true;

            case R.id.item_clear_all: {
                if (isGeofencing()) {
                    Toast.makeText(MainActivity.this, "Stop service before cleaning target areas", Toast.LENGTH_SHORT).show();
                } else {
                    deleteAllTargetAreas();
                }
            }
            return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllAllowed = true;
            for (int i = 0; i < grantResults.length; i++) {
                isAllAllowed = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }

            if (!isAllAllowed) {
                Toast.makeText(MainActivity.this, "All permissions are needed for app to work properly", Toast.LENGTH_SHORT).show();
                MainActivity.this.finish();

            } else {
                permissionsGranted();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        progressBar.setVisibility(View.INVISIBLE);

        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            moveToMyLocation();
        }

        if (targets != null) {
            addAllTargetsToMap();
        }

        initMapListeners(googleMap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        app = (LocationMonitorApp) getApplication();
        geofenceManager = app.getGeofenceManager();

        areaFillColor = ActivityCompat.getColor(this, R.color.area_fill_color);
        boundColor = ActivityCompat.getColor(this, R.color.bound_color);
        radius = getResources().getInteger(R.integer.radius);
        zoom = getResources().getInteger(R.integer.zoom);

        targets = geofenceManager.readTargets();

        progressBar = (ProgressBar) findViewById(R.id.pb_progress);
        progressBar.setVisibility(View.VISIBLE);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        verifyPermissions();
    }

    @Override
    protected void onDestroy() {
        if (map != null) {
            map.clear();
        }

        targets = new ArrayList<>();
        app.releaseDBManager();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            if (Utils.isLocationEnabled(this)) {
                onLocationEnabled();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void permissionsGranted() {
        checkLocationEnabled();
    }

    private void deleteAllTargetAreas() {
        targets.clear();

        if (map != null) {
            map.clear();
        }

        geofenceManager.clearAll();
    }

    private void onLocationEnabled() {
        mapFragment.getMapAsync(this);
        canStartService = true;
    }

    private void updateMenuItemState() {
        if (isGeofencing()) {
            itemPlayStop.setIcon(R.drawable.ic_stop_24dp);
        } else {
            itemPlayStop.setIcon(R.drawable.ic_play_24dp);
        }
    }

    private void toggleLocationService() {
        toggleLocationMonitorService();
    }

    private void verifyPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

            } else {
                permissionsGranted();
            }

        } else {
            permissionsGranted();
        }
    }

    private void checkLocationEnabled() {
        if (!Utils.isLocationEnabled(this)) {
            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION);
        }

        onLocationEnabled();
    }

    private void initMapListeners(GoogleMap googleMap) {
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addTargetArea(latLng);
            }
        });
    }

    private void addAllTargetsToMap() {
        for (TargetArea targetArea : targets) {
            map.addMarker(new MarkerOptions()
                    .position(targetArea.getAreaCenter())
                    .title(targetArea.getAreaName()));

            map.addCircle(new CircleOptions()
                    .center(targetArea.getAreaCenter())
                    .radius(targetArea.getRadius())
                    .strokeColor(boundColor)
                    .fillColor(areaFillColor));
        }
    }

    private void addTargetArea(LatLng latLng) {
        boolean isExistingArea = isInsideExistingArea(latLng);
        String areaName = "Area " + String.valueOf(counter);
        counter++;

        if (!isExistingArea) {
            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(areaName));

            map.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(radius)
                    .strokeColor(boundColor)
                    .fillColor(areaFillColor));

            TargetArea targetArea = new TargetArea(areaName, latLng, radius);
            targets.add(targetArea);

            addGeofence(targetArea);
        }
    }

    private void addGeofence(@NonNull TargetArea targetArea) {
        geofenceManager.addTarget(targetArea);
    }

    private boolean isInsideExistingArea(LatLng newTargetCenter) {
        for (TargetArea targetArea : targets) {
            double distance = Utils.distance(newTargetCenter.latitude, newTargetCenter.longitude, targetArea.getAreaCenter().latitude, targetArea.getAreaCenter().longitude);

            if (distance <= radius + targetArea.getRadius()) {
                return true;
            }
        }

        return false;
    }

    private void moveToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
            if (location == null) {
                location = map.getMyLocation();
            }

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom);
                map.animateCamera(yourLocation);
            }
        }
    }

    private void toggleLocationMonitorService() {
        if (isGeofencing()) {
            stopGeofencing();

        } else {
            startGeofencing();
        }
    }

    /**
     * Utils code
     */

    private void startGeofencing() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Location permission required", Toast.LENGTH_LONG).show();
            return;
        }

        if (app.getGeofenceManager().containsGeofences()) {
            LocationServices.GeofencingApi.addGeofences(
                    app.getGoogleApiClient(),
                    app.getGeofenceManager().getGeofencingRequest(),
                    app.getGeofenceManager().getGeofencePendingIntent()
            ).setResultCallback(getGeofenceAPIResultCallback(true));

            setGeofensingStatus(true);
        }
    }

    private void stopGeofencing() {
        LocationServices.GeofencingApi.removeGeofences(
                app.getGoogleApiClient(),
                app.getGeofenceManager().getGeofencePendingIntent()
        ).setResultCallback(getGeofenceAPIResultCallback(false));

        setGeofensingStatus(false);
    }

    @NonNull
    private ResolvingResultCallbacks<Status> getGeofenceAPIResultCallback(final boolean isStart) {
        return new ResolvingResultCallbacks<Status>(MainActivity.this, GEOFENCE_REQUEST_CODE) {
            @Override
            public void onSuccess(@NonNull Status status) {
                String message = getResources().getString(R.string.result_success);
                String action = (isStart) ? "added" : "removed";
                message = String.format(message, action);

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnresolvableFailure(@NonNull Status status) {
                String message = getResources().getString(R.string.result_failed);
                String action = (isStart) ? "added" : "removed";
                message = String.format(message, action);

                Toast.makeText(MainActivity.this, message + " Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    public boolean isGeofencing() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(SERVICE_IS_RUNNING, false);
    }

    private void setGeofensingStatus(boolean isRunning) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences
                .edit()
                .putBoolean(SERVICE_IS_RUNNING, isRunning)
                .commit();
    }

}