package com.romio.locationtest.ui.pojo;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.romio.locationtest.data.ZoneType;

/**
 * Created by roman on 1/9/17
 */

public class Area {
    private String areaName;
    private LatLng areaCenter;
    private ZoneType zoneType;
    private int radius;

    public Area(String areaName, LatLng areaCenter, ZoneType zoneType, int radius) {
        this.areaName = areaName;
        this.areaCenter = areaCenter;
        this.radius = radius;
        this.zoneType = zoneType;
    }

    public Area() { }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public LatLng getAreaCenter() {
        return areaCenter;
    }

    public void setAreaCenter(LatLng areaCenter) {
        this.areaCenter = areaCenter;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public void setZoneType(ZoneType zoneType) {
        this.zoneType = zoneType;
    }

    @Override
    public int hashCode() {
        return (areaName + radius).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Area) {
            Area ta2 = (Area) obj;
            return TextUtils.equals(ta2.areaName, this.areaName) && ta2.radius == this.radius;
        }
        return false;
    }
}
