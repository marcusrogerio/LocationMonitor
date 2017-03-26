package com.romio.locationtest.data;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by roman on 1/12/17
 */

@DatabaseTable(tableName = "area")
public class AreaDto {
    public static final String NAME_FIELD = "name";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String DATE_CREATED_FIELD = "date_created";
    public static final String ID_FIELD = "id";
    public static final String RADIUS_FIELD = "radius";
    public static final String LATITUDE_FIELD = "latitude";
    public static final String LONGITUDE_FIELD = "longitude";
    public static final String ENABLED_FIELD = "enabled";
    public static final String TYPE_FIELD = "zone_type";

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

    @DatabaseField(columnName = TYPE_FIELD)
    private ZoneType zoneType;

    public AreaDto() { }

    public AreaDto(String id, String name, double latitude, double longitude, int radius, ZoneType zoneType) {
        this.id = id;
        this.areaName = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.zoneType = zoneType;
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

    public ZoneType getZoneType() {
        return zoneType;
    }

    public void setZoneType(ZoneType zoneType) {
        this.zoneType = zoneType;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AreaDto) {
            AreaDto ta2 = (AreaDto) obj;
            return TextUtils.equals(ta2.id, this.id);
        }
        return false;
    }
}
