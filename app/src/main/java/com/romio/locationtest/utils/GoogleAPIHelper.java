package com.romio.locationtest.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.romio.locationtest.LocationMonitorApp;

/**
 * Created by roman on 3/26/17
 */

public class GoogleAPIHelper {

    private static final String TAG = GoogleAPIHelper.class.getSimpleName();
    private GoogleApiClient googleApiClient;
    private LocationMonitorApp app;
    private GoogleAPIHelperCallback callback;

    public GoogleAPIHelper(GoogleAPIHelperCallback callback) {
        this.callback = callback;
    }

    public void buildApiClient(Context context) {
        app = (LocationMonitorApp) context.getApplicationContext();

        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionCallback)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    private GoogleApiClient.ConnectionCallbacks connectionCallback = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@org.jetbrains.annotations.Nullable Bundle bundle) {
            app.setGoogleApiClient(googleApiClient);
            googleApiClient.connect();

            callback.onConnected();
        }

        @Override
        public void onConnectionSuspended(int i) {
            callback.onConnectionSuspended();
            Log.d(TAG, "Connection Suspended");
        }
    };

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            callback.onError(connectionResult.getErrorMessage());
            Log.e(TAG, "Can't start Geofencing " + connectionResult.getErrorMessage());
        }
    };
}
