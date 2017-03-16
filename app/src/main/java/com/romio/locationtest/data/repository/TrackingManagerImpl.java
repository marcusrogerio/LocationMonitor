package com.romio.locationtest.data.repository;

import android.content.Context;
import android.location.Location;
import android.provider.Settings.Secure;
import android.util.Log;

import com.romio.locationtest.data.AreaAction;
import com.romio.locationtest.data.TargetAreaDto;
import com.romio.locationtest.data.TrackingDto;
import com.romio.locationtest.data.TrackingMapper;
import com.romio.locationtest.data.db.DBHelper;
import com.romio.locationtest.data.net.KolejkaTrackingAPI;
import com.romio.locationtest.data.net.entity.BulkTracking;
import com.romio.locationtest.data.net.entity.TrackingEntity;
import com.romio.locationtest.utils.NetUtils;
import com.romio.locationtest.utils.NetworkManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

/**
 * Created by roman on 3/8/17
 */

public class TrackingManagerImpl implements TrackingManager {

    private static final String TAG = TrackingManagerImpl.class.getSimpleName();
    private NetworkManager networkManager;
    private DBHelper dbHelper;
    private KolejkaTrackingAPI kolejkaTrackingAPI;
    private String phoneId;

    public TrackingManagerImpl(Context context, NetworkManager networkManager, DBHelper dbHelper) {
        this.networkManager = networkManager;
        this.dbHelper = dbHelper;
        this.phoneId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    @Override
    public void enterArea(TargetAreaDto area, Location location) {
        Calendar calendar = Calendar.getInstance();

        TrackingDto trackingDto = new TrackingDto();
        trackingDto.setLongitude(location.getLongitude());
        trackingDto.setLatitude(location.getLatitude());
        trackingDto.setZoneId(area.getId());
        trackingDto.setDataType(AreaAction.ENTER.getActionName());
        trackingDto.setTrackingId(phoneId);
        trackingDto.setTrackingTimeStamp(calendar.getTimeInMillis());

        commitTracking(trackingDto);
    }

    @Override
    public void leaveArea(TargetAreaDto area, Location location) {
        Calendar calendar = Calendar.getInstance();

        TrackingDto trackingDto = new TrackingDto();
        trackingDto.setLongitude(location.getLongitude());
        trackingDto.setLatitude(location.getLatitude());
        trackingDto.setZoneId(area.getId());
        trackingDto.setDataType(AreaAction.LEAVE.getActionName());
        trackingDto.setTrackingId(phoneId);
        trackingDto.setTrackingTimeStamp(calendar.getTimeInMillis());

        commitTracking(trackingDto);
    }

    @Override
    public void changeArea(TargetAreaDto oldArea, TargetAreaDto newArea, Location location) {
        leaveArea(oldArea, location);
        enterArea(newArea, location);
    }

    @Override
    public void wanderInArea(TargetAreaDto area, Location location) {
        Calendar calendar = Calendar.getInstance();

        TrackingDto trackingDto = new TrackingDto();
        trackingDto.setLongitude(location.getLongitude());
        trackingDto.setLatitude(location.getLatitude());
        trackingDto.setZoneId(area.getId());
        trackingDto.setDataType(AreaAction.TRACK.getActionName());
        trackingDto.setTrackingId(phoneId);
        trackingDto.setTrackingTimeStamp(calendar.getTimeInMillis());

        commitTracking(trackingDto);
    }

    private void commitTracking(TrackingDto trackingDto) {
        if (networkManager.isNetworkAvailable()) {
            sendTracking(trackingDto);

        } else {
            saveTrackingInDB(trackingDto);
        }
    }

    private void saveTrackingInDB(TrackingDto trackingDto) {
        try {
            Log.d(TAG, "No network, save data to DB");
            dbHelper.getDbManager().getTrackingDao().createOrUpdate(trackingDto);

        } catch (SQLException e) {
            Log.e(TAG, "Error adding tracking to DB", e);
        }
    }

    private void sendTracking(TrackingDto trackingDto) {
        initNetAPI();

        try {
            if (isTrackingInDB()) {
                Log.d(TAG, "DB contain tracking info, should send it also");

                List<TrackingDto> oldTrackingDtos = getTrackingFromDB();
                oldTrackingDtos.add(trackingDto);

                sendBulkTracking(oldTrackingDtos);

            } else {
                Log.d(TAG, "DB doesn't contain tracking info, send only fresh info");

                makeCall(trackingDto);
            }

        } catch (SQLException e) {
            Log.e(TAG, "Error adding tracking to DB", e);
            throw new RuntimeException(e);
        }
    }

    private void makeCall(TrackingDto trackingDto) {
        TrackingEntity trackingEntity = TrackingMapper.map(trackingDto);
        try {
            kolejkaTrackingAPI.sendTracking(trackingEntity).execute();
            Log.d(TAG, "Successfully sent");

        } catch (IOException e) {
            Log.e(TAG, "Error making send tracking call", e);
            saveTrackingInDB(trackingDto);
        }
    }

    private void sendBulkTracking(List<TrackingDto> oldTrackingDtos) {
        List<TrackingEntity> trackingEntities = TrackingMapper.map(oldTrackingDtos);
        BulkTracking bulkTracking = new BulkTracking(trackingEntities);

        try {
            kolejkaTrackingAPI.sendBulkTracking(bulkTracking).execute();
            Log.d(TAG, "Successfully sent");

            dbHelper.getDbManager().clearTracking();
            dbHelper.release();

        } catch (IOException e) {
            Log.e(TAG, "Error making send bulk tracking call", e);
        }
    }

    private boolean isTrackingInDB() throws SQLException {
        return dbHelper.getDbManager().getTrackingDao().countOf() > 0;
    }

    private List<TrackingDto> getTrackingFromDB() throws SQLException {
        return dbHelper.getDbManager().getTrackingDao().queryForAll();
    }

    private void initNetAPI() {
        Log.d(TAG, "initNetAPI");
        if (kolejkaTrackingAPI == null) {
            kolejkaTrackingAPI = NetUtils.getRetrofit().create(KolejkaTrackingAPI.class);
        }
    }
}
