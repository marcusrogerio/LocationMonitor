package com.romio.locationtest.data.db;

/**
 * Created by roman on 3/12/17
 */

public interface DBHelper {

    DBManager getDbManager();

    void release();
}
