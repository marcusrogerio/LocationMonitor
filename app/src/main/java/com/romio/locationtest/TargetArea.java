package com.romio.locationtest;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by roman on 1/9/17
 */

public class TargetArea implements Parcelable {
    private String areaName;
    private LatLng areaCenter;
    private int radius;

    public TargetArea(String areaName, LatLng areaCenter, int radius) {
        this.areaName = areaName;
        this.areaCenter = areaCenter;
        this.radius = radius;
    }

    public TargetArea() {
    }

    protected TargetArea(Parcel in) {
        areaName = in.readString();
        areaCenter = in.readParcelable(LatLng.class.getClassLoader());
        radius = in.readInt();
    }

    public static final Creator<TargetArea> CREATOR = new Creator<TargetArea>() {
        @Override
        public TargetArea createFromParcel(Parcel in) {
            return new TargetArea(in);
        }

        @Override
        public TargetArea[] newArray(int size) {
            return new TargetArea[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(areaName);
        dest.writeParcelable(areaCenter, flags);
        dest.writeInt(radius);
    }

    @Override
    public int hashCode() {
        return (areaName + radius).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetArea) {
            TargetArea ta2 = (TargetArea) obj;
            return TextUtils.equals(ta2.areaName, this.areaName) && ta2.radius == this.radius;
        }
        return false;
    }
}
