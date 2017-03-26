package com.romio.locationtest.data.repository;

import android.location.Location;

import com.romio.locationtest.data.AreaDto;

/**
 * Created by roman on 3/8/17
 */

public interface TrackingManager {

    void enterArea(AreaDto newTargetArea, Location location);

    void leaveArea(AreaDto lastArea, Location location);

    void changeArea(AreaDto lastArea, AreaDto newTargetArea, Location location);

    void wanderInArea(AreaDto newTargetArea, Location location);
}
