package com.romio.locationtest.data;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by roman on 1/12/17
 */

@DatabaseTable(tableName = "area")
public class TargetAreaDto {
    private static final String NAME_FIELD = "name";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String DATE_CREATED_FIELD = "date_created";
    private static final String ID_FIELD = "id";
    private static final String RADIUS_FIELD = "radius";
    private static final String LATITUDE_FIELD = "latitude";
    private static final String LONGITUDE_FIELD = "longitude";
    private static final String ENABLED_FIELD = "enabled";

    @DatabaseField(id = true, columnName = ID_FIELD)
    private String id;

    @DatabaseField(columnName = NAME_FIELD)
    private String areaName;

    @DatabaseField(columnName = LATITUDE_FIELD)
    private double latitude;

    @DatabaseField(columnName = LONGITUDE_FIELD)
    private double longitude;

    @DatabaseField(columnName = RADIUS_FIELD)
    private int radius;

    @DatabaseField(columnName = DESCRIPTION_FIELD)
    private String  description;

    @DatabaseField(columnName = DATE_CREATED_FIELD)
    private Date dateCreated;

    @DatabaseField(columnName = ENABLED_FIELD)
    private boolean enabled;

    public TargetAreaDto() { }

    public TargetAreaDto(String id, String name, double latitude, double longitude, int radius) {
        this.id = id;
        this.areaName = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetAreaDto) {
            TargetAreaDto ta2 = (TargetAreaDto) obj;
            return TextUtils.equals(ta2.id, this.id);
        }
        return false;
    }
}
