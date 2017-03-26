package com.romio.locationtest.ui;

import com.romio.locationtest.data.AreaDto;

import java.util.List;

/**
 * Created by roman on 3/8/17
 */

public interface MainView {

    void addArea(AreaDto targetArea);

    void clearAreas();

    void showError(String message);

    void onAreasLoaded(List<AreaDto> areaDtos);
}
