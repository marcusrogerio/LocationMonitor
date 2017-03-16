package com.romio.locationtest.data.net;

import com.romio.locationtest.data.net.entity.BulkTracking;
import com.romio.locationtest.data.net.entity.BaseResponse;
import com.romio.locationtest.data.net.entity.TrackingEntity;
import com.romio.locationtest.data.net.entity.ZoneEntity;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by roman on 3/5/17
 */

public interface KolejkaZonesAPI {

    @POST("zones")
    Observable<ZoneEntity> addZone(@Body ZoneEntity zone);

    @GET("zones")
    Observable<BaseResponse<List<ZoneEntity>>> getZones();

    @DELETE("zones/{zone_id}")
    Observable<BaseResponse<Object>> deleteZone(@Field("zone_id") String zoneId);

    @POST("tracking")
    Observable<BaseResponse<TrackingEntity>> sendTracking(@Body TrackingEntity trackingEntity);

    @POST("bulk_tracking")
    Observable<BaseResponse<List<TrackingEntity>>> sendBulkTracking(@Body BulkTracking bulkTracking);
}
