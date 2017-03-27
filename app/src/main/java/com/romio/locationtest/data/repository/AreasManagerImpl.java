package com.romio.locationtest.data.repository;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.romio.locationtest.data.AreaDto;
import com.romio.locationtest.data.TargetAreaMapper;
import com.romio.locationtest.data.ZoneType;
import com.romio.locationtest.data.db.DBHelper;
import com.romio.locationtest.data.db.DBManager;
import com.romio.locationtest.data.net.KolejkaZonesAPI;
import com.romio.locationtest.data.net.entity.BaseResponse;
import com.romio.locationtest.data.net.entity.ZoneEntity;
import com.romio.locationtest.utils.NetUtils;
import com.romio.locationtest.utils.NetworkManager;
import com.romio.locationtest.utils.RxUtils;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by roman on 3/8/17
 */

public class AreasManagerImpl implements AreasManager {
    private static final String TAG = AreasManagerImpl.class.getSimpleName();

    private DBHelper dbHelper;
    private KolejkaZonesAPI kolejkaZonesAPI;
    private NetworkManager networkManager;
    private Context context;

    public AreasManagerImpl(Context context, DBHelper dbHelper, NetworkManager networkManager) {
        this.dbHelper = dbHelper;
        this.networkManager = networkManager;
        this.context = context;
    }

    @Override
    public Observable<List<AreaDto>> loadAllAreas() {
        if (networkManager.isNetworkAvailable()) {
            return getAllAreasFromNet();

        } else {
            return Observable.just(getAllAreasFromDB());
        }
    }

    @Override
    public List<AreaDto> getCheckpointsFromDB() {
        DBManager dbManager = dbHelper.getDbManager();
        try {
            return dbManager.getAreaDao().queryForEq(AreaDto.TYPE_FIELD, ZoneType.CHECKPOINT);

        } catch (SQLException e) {
            Log.e(TAG, "Error reading targets from DB", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AreaDto> getAllAreasFromDB() {
        DBManager dbManager = dbHelper.getDbManager();
        try {
            return dbManager.getAreaDao().queryForAll();

        } catch (SQLException e) {
            Log.e(TAG, "Error reading targets from DB", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AreaDto> getGeofenceAreasFromDB() {
        DBManager dbManager = dbHelper.getDbManager();
        try {
            return dbManager.getAreaDao().queryForEq(AreaDto.TYPE_FIELD, ZoneType.CONTROL);

        } catch (SQLException e) {
            Log.e(TAG, "Error reading targets from DB", e);
            throw new RuntimeException(e);
        }
    }

    private Observable<List<AreaDto>> getAllAreasFromNet() {
        initNetAPI();

        return kolejkaZonesAPI
                .getZones()
                .compose(RxUtils.<BaseResponse<List<ZoneEntity>>>applySchedulers())
                .map(new Func1<BaseResponse<List<ZoneEntity>>, List<AreaDto>>() {
                    @Override
                    public List<AreaDto> call(BaseResponse<List<ZoneEntity>> listBaseResponse) {
                        List<AreaDto> areaDtos = TargetAreaMapper.map(listBaseResponse.getData());
                        filterEnabledAreas(areaDtos);
                        updateAreasInDB(areaDtos);
                        sendBroadCastAboutNewAreas();
                        return areaDtos;
                    }
                });
    }

    private void filterEnabledAreas(List<AreaDto> areaDtos) {
        if (areaDtos != null) {
            Iterator<AreaDto> iterator = areaDtos.iterator();
            while (iterator.hasNext()) {
                AreaDto areaDto = iterator.next();
                if (!areaDto.isEnabled()) {
                    iterator.remove();
                }
            }
        }
    }

    private void sendBroadCastAboutNewAreas() {
        Intent intent = new Intent("com.kolejka.areas.loaded");
        context.sendBroadcast(intent);
    }

    private void updateAreasInDB(List<AreaDto> areaDtos) {
        DBManager dbManager = dbHelper.getDbManager();
        try {
            dbManager.clearAreas();

            for (AreaDto areaDto : areaDtos) {
                dbManager.getAreaDao().createOrUpdate(areaDto);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error adding area to DB", e);
            throw new RuntimeException(e);
        }
    }

    private void initNetAPI() {
        if (kolejkaZonesAPI == null) {
            kolejkaZonesAPI = NetUtils.getRxRetrofit().create(KolejkaZonesAPI.class);
        }
    }
}
