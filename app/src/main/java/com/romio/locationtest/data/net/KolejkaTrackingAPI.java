package com.romio.locationtest.data.net;

import com.romio.locationtest.data.net.entity.BulkTracking;
import com.romio.locationtest.data.net.entity.BaseResponse;
import com.romio.locationtest.data.net.entity.TrackingEntity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by roman on 3/5/17
 */

public interface KolejkaTrackingAPI {

    @POST("tracking")
    Call<BaseResponse<TrackingEntity>> sendTracking(@Body TrackingEntity trackingEntity);

    @POST("bulk_tracking")
    Call<BaseResponse<List<TrackingEntity>>> sendBulkTracking(@Body BulkTracking bulkTracking);
}
