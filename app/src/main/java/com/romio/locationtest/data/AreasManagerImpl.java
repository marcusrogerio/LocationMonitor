package com.romio.locationtest.data;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.romio.locationtest.data.db.DBManager;
import com.romio.locationtest.data.db.DataBaseHelper;
import com.romio.locationtest.data.net.KolejkaZonesAPI;
import com.romio.locationtest.data.net.entity.GeneralResponse;
import com.romio.locationtest.data.net.entity.ZoneEntity;
import com.romio.locationtest.utils.NetUtils;
import com.romio.locationtest.utils.NetworkManager;

import java.sql.SQLException;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by roman on 3/8/17
 */

public class AreasManagerImpl implements AreasManager {
    private static final String TAG = AreasManagerImpl.class.getSimpleName();

    private Context context;
    private KolejkaZonesAPI kolejkaZonesAPI;
    private DataBaseHelper databaseHelper = null;
    private NetworkManager networkManager;

    public AreasManagerImpl(Context context, NetworkManager networkManager) {
        this.context = context;
        this.networkManager = networkManager;
    }

    @Override
    public void releaseDBManager() {
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    @Override
    public Observable<List<TargetAreaDto>> loadTargetAreas() {
        if (networkManager.isNetworkAvailable()) {
            return getAreasFromNet();

        } else {
            return getAreasFromDB();

        }
    }

    @Override
    public List<TargetAreaDto> getTargetAreasFromDB() {
        DBManager dbManager = getDBManager();
        try {
            return dbManager.getAreaDao().queryForAll();

        } catch (SQLException e) {
            Log.e(TAG, "Error reading targets from DB", e);
            throw new RuntimeException(e);
        }
    }

    private Observable<List<TargetAreaDto>> getAreasFromDB() {
        return Observable.just(getTargetAreasFromDB());
    }

    private Observable<List<TargetAreaDto>> getAreasFromNet() {
        initNetAPI();

        return kolejkaZonesAPI.getZones().map(new Func1<GeneralResponse<List<ZoneEntity>>, List<TargetAreaDto>>() {
            @Override
            public List<TargetAreaDto> call(GeneralResponse<List<ZoneEntity>> listGeneralResponse) {
                List<TargetAreaDto> targetAreaDtos = TargetAreaMapper.map(listGeneralResponse.getData());
                updateAreasInDB(targetAreaDtos);
                return targetAreaDtos;
            }
        });
    }

    private void updateAreasInDB(List<TargetAreaDto> targetAreaDtos) {
        DBManager dbManager = getDBManager();
        for (TargetAreaDto targetAreaDto : targetAreaDtos) {
            try {
                dbManager.getAreaDao().createOrUpdate(targetAreaDto);

            } catch (SQLException e) {
                Log.e(TAG, "Error adding area to DB", e);
            }
        }
    }

    private void initNetAPI() {
        if (kolejkaZonesAPI == null) {
            kolejkaZonesAPI = NetUtils.getRetrofit().create(KolejkaZonesAPI.class);
        }
    }

    private DBManager getDBManager() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(context, DataBaseHelper.class);
        }

        return databaseHelper;
    }
}
