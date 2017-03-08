package com.romio.locationtest.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.romio.locationtest.data.TargetAreaDto;

import java.sql.SQLException;

/**
 * Created by roman on 1/12/17
 */

public class DataBaseHelper extends OrmLiteSqliteOpenHelper implements DBManager {

    private final static String TAG = DataBaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "area_db";
    private static final int DATABASE_VERSION = 1;

    private Dao<TargetAreaDto, String> areasDao = null;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(TAG, "DB creation");
            TableUtils.createTable(connectionSource, TargetAreaDto.class);

        } catch (SQLException e) {
            Log.e(TAG, "Can't create database", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(TAG, "DB upgrade");
            TableUtils.dropTable(connectionSource, TargetAreaDto.class, true);

            onCreate(database, connectionSource);

        } catch (SQLException e) {
            Log.e(TAG, "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Dao<TargetAreaDto, String> getAreaDao() throws SQLException {
        if (areasDao == null) {
            areasDao = getDao(TargetAreaDto.class);
        }

        return areasDao;
    }

    @Override
    public void clearAll() {
        try {
            TableUtils.clearTable(getConnectionSource(), TargetAreaDto.class);

        } catch (SQLException e) {
            Log.e(TAG, "Can't delete table", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        super.close();
        areasDao = null;
    }
}
