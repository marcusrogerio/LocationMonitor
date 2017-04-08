package com.romio.locationtest.data.repository;

import com.romio.locationtest.data.AreaDto;

import java.util.List;

import rx.Observable;

/**
 * Created by roman on 3/8/17
 */

public interface AreasManager {
    Observable<List<AreaDto>> loadAllAreas();

    Observable<Boolean> updateAreas();

    List<AreaDto> getCheckpointsFromDB();

    List<AreaDto> getAllAreasFromDB();

    List<AreaDto> getGeofenceAreasFromDB();
}
