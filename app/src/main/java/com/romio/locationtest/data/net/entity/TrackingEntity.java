package com.romio.locationtest.data.net.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by roman on 3/8/17
 */

public class TrackingEntity {

    @SerializedName("data_type")
    private String dataType;

    @SerializedName("zone_id")
    private String zoneId;

    @SerializedName("zone_type")
    private String zoneType;

    @SerializedName("tracking_id")
    private String trackingId;

    @SerializedName("tracking_timestamp")
    private long trackingTimestamp;

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public long getTrackingTimestamp() {
        return trackingTimestamp;
    }

    public void setTrackingTimestamp(long trackingTimestamp) {
        this.trackingTimestamp = trackingTimestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getZoneType() {
        return zoneType;
    }

    public void setZoneType(String zoneType) {
        this.zoneType = zoneType;
    }
}