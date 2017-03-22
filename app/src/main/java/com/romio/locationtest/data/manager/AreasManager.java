package com.romio.locationtest.data.manager;

import com.romio.locationtest.data.TargetAreaDto;

import java.util.List;

import rx.Observable;

/**
 * Created by roman on 3/8/17
 */

public interface AreasManager {
    Observable<List<TargetAreaDto>> loadTargetAreas();

    List<TargetAreaDto> getTargetAreasFromDB();
}
