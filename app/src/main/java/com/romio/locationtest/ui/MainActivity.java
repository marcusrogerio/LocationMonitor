package com.romio.locationtest.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.R;
import com.romio.locationtest.data.AreaDto;
import com.romio.locationtest.data.ZoneType;
import com.romio.locationtest.utils.LocationUtils;

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

    private int zoom;
    private int checkpointAreaFillColor;
    private int checkpointBoundColor;
    private int controlAreaFillColor;
    private int controlBoundColor;

    boolean canStartService = false;

    public static void startActivity(SplashActivity splashActivity) {
        Intent intent = new Intent(splashActivity, MainActivity.class);
        splashActivity.startActivity(intent);
        splashActivity.finish();
    }

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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item_info: {
                showInfoDialog();
            }
            return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showInfoDialog() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String infoPattern = getResources().getString(R.string.info_message_pattern);
        String message = String.format(infoPattern, androidId);

        new AlertDialog.Builder(this).setCancelable(true)
                .setTitle(R.string.info_title)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
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
            if (LocationUtils.isLocationEnabled(this)) {
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

    @Override
    public void addArea(AreaDto areaDto) {
        LatLng center = new LatLng(areaDto.getLatitude(), areaDto.getLongitude());

        map.addMarker(new MarkerOptions()
                .position(center)
                .title(areaDto.getAreaName()));

        int strokeColor = (areaDto.getZoneType() == ZoneType.CHECKPOINT) ? checkpointBoundColor : controlBoundColor;
        int fillColor = (areaDto.getZoneType() == ZoneType.CHECKPOINT) ? checkpointAreaFillColor : controlAreaFillColor;

        map.addCircle(new CircleOptions()
                .center(center)
                .radius(areaDto.getRadius())
                .strokeColor(strokeColor)
                .fillColor(fillColor));
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
    public void onAreasLoaded(List<AreaDto> areaDtos) {
        for (AreaDto targetArea : areaDtos) {
            if (targetArea.isEnabled()) {
                addArea(targetArea);
            }
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

    private void permissionsGranted() {
        checkLocationEnabled();
    }

    private void initValues() {
        zoom = getResources().getInteger(R.integer.zoom);
        checkpointAreaFillColor = ActivityCompat.getColor(this, R.color.checkpoint_area_fill_color);
        checkpointBoundColor = ActivityCompat.getColor(this, R.color.checkpoint_bound_color);

        controlAreaFillColor = ActivityCompat.getColor(this, R.color.control_area_fill_color);
        controlBoundColor = ActivityCompat.getColor(this, R.color.control_bound_color);
    }

    private void onLocationEnabled() {
        mapFragment.getMapAsync(this);
        canStartService = true;
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
        if (!LocationUtils.isLocationEnabled(this)) {
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
}