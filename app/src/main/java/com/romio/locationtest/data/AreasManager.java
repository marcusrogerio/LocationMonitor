package com.romio.locationtest.data;

import java.util.List;

import rx.Observable;

/**
 * Created by roman on 3/8/17
 */

public interface AreasManager {
    void releaseDBManager();

    Observable<List<TargetAreaDto>> loadTargetAreas();

    List<TargetAreaDto> getTargetAreasFromDB();
}
