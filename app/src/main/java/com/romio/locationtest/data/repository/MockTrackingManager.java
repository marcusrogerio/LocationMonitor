package com.romio.locationtest.data.repository;

import android.content.Context;
import android.location.Location;

import com.romio.locationtest.data.AreaDto;
import com.romio.locationtest.utils.NotificationUtils;

/**
 * Created by roman on 3/24/17
 */

public class MockTrackingManager implements TrackingManager {
    private Context context;

    public MockTrackingManager(Context context) {
        this.context = context;
    }

    @Override
    public void enterArea(AreaDto newTargetArea, Location location) {
        String message = "Entered " + newTargetArea.getAreaName();
        String title = "Area status changed";

        NotificationUtils.notifyUser(context, message, title);
    }

    @Override
    public void leaveArea(AreaDto lastArea, Location location) {
        String message = "Leaved " + lastArea.getAreaName();
        String title = "Area status changed";

        NotificationUtils.notifyUser(context, message, title);
    }

    @Override
    public void changeArea(AreaDto lastArea, AreaDto newTargetArea, Location location) {
        String message = "Areas changed from " + lastArea.getAreaName() + " to " + newTargetArea.getAreaName();
        String title = "Areas changed";

        NotificationUtils.notifyUser(context, message, title);
    }

    @Override
    public void wanderInArea(AreaDto targetArea, Location location) {
        String title = "Presence in Area";
        String message = targetArea.getAreaName() + ".\n Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude();

        NotificationUtils.notifyUser(context, message, title);
    }
}
