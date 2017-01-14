package com.romio.locationtest;

import android.location.Location;

/**
 * Created by roman on 1/14/17.
 */

public class Utils {

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

    public static boolean isInside(TargetArea targetArea, Location location) {
        double distance = distance(
                targetArea.getAreaCenter().latitude,
                targetArea.getAreaCenter().longitude,
                location.getLatitude(),
                location.getLongitude());

        return distance <= targetArea.getRadius();
    }
}
