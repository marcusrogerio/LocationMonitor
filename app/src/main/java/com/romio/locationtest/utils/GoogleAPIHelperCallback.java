package com.romio.locationtest.utils;

/**
 * Created by roman on 3/26/17
 */

public interface GoogleAPIHelperCallback {
    void onError(String errorMessage);

    void onConnected();

    void onConnectionSuspended();
}
