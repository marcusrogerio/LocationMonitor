package com.romio.locationtest.data;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by roman on 1/12/17.
 */

@DatabaseTable(tableName = "area")
public class TargetAreaDto {
    public static final String NAME_FIELD = "name";
    public static final String RADIUS_FIELD = "radius";
    public static final String LATITUDE_FIELD = "latitude";
    public static final String LONGITUDE_FIELD = "longitude";

    @DatabaseField(id = true)
    private String id;

    @DatabaseField(columnName = NAME_FIELD)
    private String areaName;

    @DatabaseField(columnName = LATITUDE_FIELD)
    private double latitude;

    @DatabaseField(columnName = LONGITUDE_FIELD)
    private double longitude;

    @DatabaseField(columnName = RADIUS_FIELD)
    private int radius;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public int hashCode() {
        return (areaName + radius).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetAreaDto) {
            TargetAreaDto ta2 = (TargetAreaDto) obj;
            return TextUtils.equals(ta2.areaName, this.areaName) && ta2.radius == this.radius;
        }
        return false;
    }
}
