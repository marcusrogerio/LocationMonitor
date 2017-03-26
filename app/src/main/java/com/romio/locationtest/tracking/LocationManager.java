package com.romio.locationtest.tracking;

/**
 * Created by roman on 3/26/17
 */

public interface LocationManager {


    void startLocationMonitorService();

    void stopLocationMonitorService();

    boolean isLocationMonitorAlarmSet();
}
