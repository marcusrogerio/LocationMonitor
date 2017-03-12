package com.romio.locationtest.data;

import android.content.Context;

import com.romio.locationtest.utils.NetworkManager;

/**
 * Created by roman on 3/8/17
 */

public class TrackingManagerImpl implements TrackingManager {

    private Context context;
    private NetworkManager networkManager;

    public TrackingManagerImpl(Context context, NetworkManager networkManager) {
        this.context = context;
        this.networkManager = networkManager;
    }

    @Override
    public void commitTracking() {
        if (networkManager.isNetworkAvailable()) {
            sendTracking();
        } else {
            saveTrackingInDB();
        }
    }

    private void saveTrackingInDB() {

    }

    private void sendTracking() {

    }
}
