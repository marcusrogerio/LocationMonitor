package com.romio.locationtest.data.net;

import com.romio.locationtest.data.net.entity.BulkTracking;
import com.romio.locationtest.data.net.entity.GeneralResponse;
import com.romio.locationtest.data.net.entity.TrackingEntity;
import com.romio.locationtest.data.net.entity.ZoneEntity;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by roman on 3/5/17
 */

public interface KolejkaTrackingAPI {

    @POST("tracking")
    GeneralResponse<TrackingEntity> sendTracking(@Body TrackingEntity trackingEntity);

    @POST("bulk_tracking")
    GeneralResponse<List<TrackingEntity>> sendBulkTracking(@Body BulkTracking bulkTracking);
}
