package com.romio.locationtest.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.romio.locationtest.data.AreaDto;

/**
 * Created by roman on 1/14/17
 */

public class LocationUtils {

    public static double distance(
            double lat1, double lng1, double lat2, double lng2) {
        int earthRadius = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double hipotenuse = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * hipotenuse * 1000;
    }

    public static boolean isInside(AreaDto targetArea, Location location) {
        double distance = distance(
                targetArea.getLatitude(),
                targetArea.getLongitude(),
                location.getLatitude(),
                location.getLongitude());

        return distance <= targetArea.getRadius();
    }


    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) |
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
