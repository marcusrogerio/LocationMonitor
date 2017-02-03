package com.romio.locationtest.data;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

/**
 * Created by roman on 1/12/17.
 */

public interface DBManager {
    Dao<TargetAreaDto, String> getAreaDao() throws SQLException;
    void clearAll();
}
