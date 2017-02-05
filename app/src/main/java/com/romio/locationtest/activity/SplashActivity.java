package com.romio.locationtest.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.R;

/**
 * Created by roman on 2/4/17
 */

public class SplashActivity extends AppCompatActivity {

    public static final String TAG = SplashActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();

        verifyGooglePlayServices(this);
        buildAndConnectApiClient();
    }

    private boolean verifyGooglePlayServices(AppCompatActivity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();

            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    private void buildAndConnectApiClient() {
        LocationMonitorApp app = (LocationMonitorApp) getApplication();
        googleApiClient = app.getGoogleApiClient();

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(connectionCallback)
                    .addOnConnectionFailedListener(onConnectionFailedListener)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        } else {
            moveToMainActivity();
        }
    }

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(SplashActivity.this, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private GoogleApiClient.ConnectionCallbacks connectionCallback = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@org.jetbrains.annotations.Nullable Bundle bundle) {
            moveToMainActivity();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Toast.makeText(SplashActivity.this, "Connection Suspended", Toast.LENGTH_SHORT).show();
        }
    };

    private void moveToMainActivity() {
        LocationMonitorApp app = (LocationMonitorApp) getApplication();
        app.setGoogleApiClient(googleApiClient);
        MainActivity.startActivity(this);
    }

}
