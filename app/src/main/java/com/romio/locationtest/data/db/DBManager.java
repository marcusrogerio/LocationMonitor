package com.romio.locationtest.data.db;

import com.j256.ormlite.dao.Dao;
import com.romio.locationtest.data.TargetAreaDto;
import com.romio.locationtest.data.TrackingDto;

import java.sql.SQLException;

/**
 * Created by roman on 1/12/17
 */

public interface DBManager {
    Dao<TargetAreaDto, String> getAreaDao() throws SQLException;

    Dao<TrackingDto, String> getTrackingDao() throws SQLException;

    void clearAreas();

    void clearTracking();

    void clearAll();
}
