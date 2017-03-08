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
import rx.Observable;

/**
 * Created by roman on 3/5/17
 */

public interface KolejkaZonesAPI {

    @POST("zones")
    Observable<ZoneEntity> addZone(@Body ZoneEntity zone);

    @GET("zones")
    Observable<GeneralResponse<List<ZoneEntity>>> getZones();

    @DELETE("zones/{zone_id}")
    Observable<GeneralResponse<Object>> deleteZone(@Field("zone_id") String zoneId);

    @POST("tracking")
    Observable<GeneralResponse<TrackingEntity>> sendTracking(@Body TrackingEntity trackingEntity);

    @POST("bulk_tracking")
    Observable<GeneralResponse<List<TrackingEntity>>> sendBulkTracking(@Body BulkTracking bulkTracking);
}
