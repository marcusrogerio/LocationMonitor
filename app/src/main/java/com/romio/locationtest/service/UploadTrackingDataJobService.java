package com.romio.locationtest.service;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.data.TrackingDto;
import com.romio.locationtest.data.TrackingMapper;
import com.romio.locationtest.data.db.DBHelper;
import com.romio.locationtest.data.net.KolejkaTrackingAPI;
import com.romio.locationtest.data.net.entity.BaseResponse;
import com.romio.locationtest.data.net.entity.BulkTracking;
import com.romio.locationtest.data.net.entity.TrackingEntity;
import com.romio.locationtest.utils.NetUtils;

import java.sql.SQLException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by roman on 3/21/17
 */

public class UploadTrackingDataJobService extends JobService {

    private static final String TAG = UploadTrackingDataJobService.class.getSimpleName();
    private KolejkaTrackingAPI kolejkaTrackingAPI;
    private DBHelper dbHelper;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        LocationMonitorApp app = (LocationMonitorApp) getApplication();
        dbHelper = app.getDBHelper();
        try {
            if (isTrackingInDB()) {
                Log.d(TAG, "DB contain tracking info, should send it also");
                kolejkaTrackingAPI = NetUtils.getRetrofit().create(KolejkaTrackingAPI.class);

                List<TrackingDto> oldTrackingDtos = getTrackingFromDB();
                sendBulkTracking(oldTrackingDtos, new BulkRequestListener() {
                    @Override
                    public void onDataUploaded(boolean isSuccess) {
                        jobFinished(jobParameters, false);
                    }
                });
                return true;

            } else {
                Log.d(TAG, "DB doesn't contain tracking info");
                return false;
            }

        } catch (SQLException e) {
            Log.e(TAG, "Error adding tracking to DB", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        LocationMonitorApp app = (LocationMonitorApp) getApplication();
        dbHelper = app.getDBHelper();

        try {
            return isTrackingInDB();

        } catch (SQLException e) {
            Log.e(TAG, "Error checking tracking entities in DB", e);
            throw new RuntimeException(e);
        }
    }

    private void sendBulkTracking(List<TrackingDto> oldTrackingDtos, final BulkRequestListener bulkRequestListener) {
        List<TrackingEntity> trackingEntities = TrackingMapper.map(oldTrackingDtos);
        BulkTracking bulkTracking = new BulkTracking(trackingEntities);


        kolejkaTrackingAPI.sendBulkTracking(bulkTracking).enqueue(new Callback<BaseResponse<List<TrackingEntity>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<TrackingEntity>>> call, Response<BaseResponse<List<TrackingEntity>>> response) {
                Log.d(TAG, "Successfully sent");

                bulkRequestListener.onDataUploaded(true);
                dbHelper.getDbManager().clearTracking();
                dbHelper.release();
            }

            @Override
            public void onFailure(Call<BaseResponse<List<TrackingEntity>>> call, Throwable throwable) {
                bulkRequestListener.onDataUploaded(false);
                Log.e(TAG, "Error making send bulk tracking call", throwable);
            }
        });
    }

    private boolean isTrackingInDB() throws SQLException {
        return dbHelper.getDbManager().getTrackingDao().countOf() > 0;
    }

    private List<TrackingDto> getTrackingFromDB() throws SQLException {
        return dbHelper.getDbManager().getTrackingDao().queryForAll();
    }

    private interface BulkRequestListener {
        void onDataUploaded(boolean isSuccess);
    }
}
