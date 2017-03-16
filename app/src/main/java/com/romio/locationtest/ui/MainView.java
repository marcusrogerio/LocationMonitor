package com.romio.locationtest.ui;

import com.romio.locationtest.data.TargetAreaDto;

import java.util.List;

/**
 * Created by roman on 3/8/17
 */

public interface MainView {

    void addArea(TargetAreaDto targetArea);

    void clearAreas();

    void showError(String message);

    void onAreasLoaded(List<TargetAreaDto> targetAreaDtos);
}
