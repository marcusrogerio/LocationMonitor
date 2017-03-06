package com.romio.locationtest.net.in;

import java.util.List;

/**
 * Created by roman on 3/7/17
 */

public class ZonesResponse {

    private List<ZoneEntity> zoneEntityList;
    private boolean status;

    public List<ZoneEntity> getZoneEntityList() {
        return zoneEntityList;
    }

    public void setZoneEntityList(List<ZoneEntity> zoneEntityList) {
        this.zoneEntityList = zoneEntityList;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
