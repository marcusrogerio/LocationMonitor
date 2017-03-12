package com.romio.locationtest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.romio.locationtest.data.TargetAreaDto;
import com.romio.locationtest.utils.CalcUtils;

import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MainView {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int PERMISSION_REQUEST_CODE = 107;
    private static final int REQUEST_ENABLE_LOCATION = 102;

    private MainPresenter presenter;

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private ProgressBar progressBar;
    private MenuItem itemPlayStop;

    private int zoom;
    private int areaFillColor;
    private int boundColor;

    boolean canStartService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationMonitorApp app = (LocationMonitorApp) getApplication();

        presenter = new MainPresenter(app.getDBHelper(), app.getAreasManager(), this);

        initValues();

        progressBar = (ProgressBar) findViewById(R.id.pb_progress);
        progressBar.setVisibility(View.VISIBLE);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        verifyGooglePlayServices();
        verifyPermissions();
    }

    @Override
    protected void onDestroy() {
        presenter.onViewDestroying();
        presenter = null;
        super.onDestroy();
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
                if (presenter.canLaunchService()) {
                    toggleLocationService();
                } else {
                    Toast.makeText(MainActivity.this, "Can't start service yet", Toast.LENGTH_SHORT).show();
                }

                updateMenuItemState();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            if (CalcUtils.isLocationEnabled(this)) {
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
        map.getUiSettings().setMapToolbarEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            moveToMyLocation();

            presenter.loadTargets();
        }
    }

    private void permissionsGranted() {
        checkLocationEnabled();
    }

    private void initValues() {
        zoom = getResources().getInteger(R.integer.zoom);
        areaFillColor = ActivityCompat.getColor(this, R.color.area_fill_color);
        boundColor = ActivityCompat.getColor(this, R.color.bound_color);
    }

    private void onLocationEnabled() {
        mapFragment.getMapAsync(this);
        canStartService = true;
    }

    private void updateMenuItemState() {
        if (((LocationMonitorApp) getApplication()).isLocationMonitorAlarmSet()) {
            itemPlayStop.setIcon(R.drawable.ic_stop_24dp);
        } else {
            itemPlayStop.setIcon(R.drawable.ic_play_24dp);
        }
    }

    private void toggleLocationService() {
        ((LocationMonitorApp) getApplication()).toggleLocationMonitorService(this);
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
        if (!CalcUtils.isLocationEnabled(this)) {
            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION);
        }

        onLocationEnabled();
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

    @Override
    public void addArea(TargetAreaDto targetArea) {
        LatLng center = new LatLng(targetArea.getLatitude(), targetArea.getLongitude());

        map.addMarker(new MarkerOptions()
                .position(center)
                .title(targetArea.getAreaName()));

        map.addCircle(new CircleOptions()
                .center(center)
                .radius(targetArea.getRadius())
                .strokeColor(boundColor)
                .fillColor(areaFillColor));
    }

    @Override
    public void clearAreas() {
        if (map != null) {
            map.clear();
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAreasLoaded(List<TargetAreaDto> targetAreaDtos) {
        for (TargetAreaDto targetArea : targetAreaDtos) {
            if (targetArea.isEnabled()) {
                addArea(targetArea);
            }
        }
    }
}