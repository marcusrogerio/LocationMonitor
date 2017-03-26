package com.romio.locationtest.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.R;
import com.romio.locationtest.utils.GoogleAPIHelper;
import com.romio.locationtest.utils.GoogleAPIHelperCallback;

/**
 * Created by roman on 3/24/17
 */

public class SplashActivity extends AppCompatActivity {

    public static final String TAG = SplashActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

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
        GoogleApiClient googleApiClient = app.getGoogleApiClient();

        if (googleApiClient == null) {
            new GoogleAPIHelper(googleAPIHelperCallback).buildApiClient(this);

        } else {
            if (!googleApiClient.isConnected()) {
                googleApiClient.connect();
            } else {
                moveToMainActivity();
            }
        }
    }

    private void moveToMainActivity() {
        MainActivity.startActivity(this);
    }

    private GoogleAPIHelperCallback googleAPIHelperCallback = new GoogleAPIHelperCallback() {
        @Override
        public void onError(String errorMessage) {
            Toast.makeText(SplashActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onConnected() {
            moveToMainActivity();
        }

        @Override
        public void onConnectionSuspended() {
            Toast.makeText(SplashActivity.this, "Connection Suspended", Toast.LENGTH_SHORT).show();
        }
    };
}