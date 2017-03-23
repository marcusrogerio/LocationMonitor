package com.romio.locationtest.data.manager;

import android.location.Location;

import com.romio.locationtest.data.TargetAreaDto;

/**
 * Created by roman on 3/8/17
 */

public interface TrackingManager {

    void enterArea(TargetAreaDto newTargetArea, Location location);

    void leaveArea(TargetAreaDto lastArea, Location location);

    void changeArea(TargetAreaDto lastArea, TargetAreaDto newTargetArea, Location location);

    void wanderInArea(TargetAreaDto newTargetArea, Location location);
}