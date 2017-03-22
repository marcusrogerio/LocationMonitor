package com.romio.locationtest.data;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by roman on 3/16/17
 */

@DatabaseTable(tableName = "tracking")
public class TrackingDto {
    private static final String DATA_TYPE_FIELD = "data_type";
    private static final String ZONE_ID_FIELD = "zone_id";
    private static final String TRACKING_ID_FIELD = "tracking_id";
    private static final String TRACKING_TIME_STAMP_FIELD = "tracking_timestamp";
    private static final String LATITUDE_FIELD = "latitude";
    private static final String LONGITUDE_FIELD = "longitude";
    private static final String ID_FIELD = "id";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private long id;

    @DatabaseField(columnName = TRACKING_ID_FIELD)
    private String trackingId;

    @DatabaseField(columnName = DATA_TYPE_FIELD)
    private String dataType;

    @DatabaseField(columnName = ZONE_ID_FIELD)
    private String zoneId;

    @DatabaseField(columnName = TRACKING_TIME_STAMP_FIELD)
    private long trackingTimeStamp;

    @DatabaseField(columnName = LATITUDE_FIELD)
    private double latitude;

    @DatabaseField(columnName = LONGITUDE_FIELD)
    private double longitude;

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

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

    public long getTrackingTimeStamp() {
        return trackingTimeStamp;
    }

    public void setTrackingTimeStamp(long trackingTimeStamp) {
        this.trackingTimeStamp = trackingTimeStamp;
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

    @Override
    public int hashCode() {
        String hashCodeSource = String.valueOf(trackingTimeStamp) + zoneId + dataType;
        return hashCodeSource.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrackingDto) {
            TrackingDto ta2 = (TrackingDto) obj;
            return (TextUtils.equals(ta2.zoneId, this.zoneId) &&
                    TextUtils.equals(ta2.dataType, this.dataType) &&
                    ta2.trackingTimeStamp == this.trackingTimeStamp);
        }
        return false;
    }
}
