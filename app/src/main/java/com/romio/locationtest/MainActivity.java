package com.romio.locationtest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.BindColor;
import butterknife.BindInt;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap map;

    private SupportMapFragment mapFragment;
    private ProgressBar progressBar;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int PERMISSION_REQUEST_CODE = 107;
    private static final int REQUEST_ENABLE_LOCATION = 102;
    private static int counter = 0;
    private boolean isServiceRunning = false;
    private int zoom = 15;

    private ArrayList<TargetArea> targets = new ArrayList<>();

    @BindInt(R.integer.radius)
    int radius;

    @BindColor(R.color.area_fill_color)
    int areaFillColor;

    @BindColor(R.color.bound_color)
    int boundColor;

    boolean canStartService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (getIntent().getParcelableArrayListExtra(LocationService.DATA) != null) {
            targets = getIntent().getParcelableArrayListExtra(LocationService.DATA);
            isServiceRunning = true;
        }

        progressBar = (ProgressBar) findViewById(R.id.pb_progress);
        progressBar.setVisibility(View.VISIBLE);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.STOP);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        verifyGooglePlayServices();
        verifyPermissions();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_launch:
                if (canStartService) {
                    startLocationService();
                } else {
                    Toast.makeText(MainActivity.this, "Can't start service yet", Toast.LENGTH_SHORT).show();
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

    private void permissionsGranted() {
        checkLocationEnabled();
    }

    private void onLocationEnabled() {
        mapFragment.getMapAsync(this);
        canStartService = true;
    }

    private void startLocationService() {
        if (!isServiceRunning) {
            Intent intent = new Intent(this, LocationService.class);
            intent.setAction(LocationService.START);

            intent.putParcelableArrayListExtra(LocationService.DATA, targets);
            startService(intent);

            isServiceRunning = true;
        }
    }

    private boolean verifyGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();

            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
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
        if (!isLocationEnabled()) {
            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION);
        }

        onLocationEnabled();
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) |
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            if (isLocationEnabled()) {
                onLocationEnabled();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        progressBar.setVisibility(View.INVISIBLE);

        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            moveToMyLocation();
        }

        if (targets != null) {
            addAllTargets();
        }

        initMapListeners(googleMap);
    }

    private void initMapListeners(GoogleMap googleMap) {
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addTargetArea(latLng);
            }
        });
    }

    private void addAllTargets() {
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


            targets.add(new TargetArea(areaName, latLng, radius));
        }
    }

    private boolean isInsideExistingArea(LatLng newTargetCenter) {
//        for (LatLng latLng : targets) {
//            double startLatitude = latLng.latitude - radius;
//            double endLatitude = latLng.latitude + radius;
//
//            double startLongitude = latLng.longitude - radius;
//            double endLongitude = latLng.longitude + radius;
//
//            if ( newTargetCenter.latitude + radius >= startLatitude &&
//                    newTargetCenter.latitude - radius <= endLatitude &&
//                    newTargetCenter.longitude + radius >= startLongitude &&
//                    newTargetCenter.longitude - radius <= endLongitude) {
//                return true;
//            }
//        }

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

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), LocationService.STOP)) {
                MainActivity.this.isServiceRunning = false;
            }
        }
    };
}