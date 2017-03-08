package com.romio.locationtest.data.net.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by roman on 3/8/17
 */

public class BulkTracking {

    @SerializedName("data")
    private List<TrackingEntity> bulkTracking;

    public List<TrackingEntity> getBulkTracking() {
        return bulkTracking;
    }

    public void setBulkTracking(List<TrackingEntity> bulkTracking) {
        this.bulkTracking = bulkTracking;
    }
}
