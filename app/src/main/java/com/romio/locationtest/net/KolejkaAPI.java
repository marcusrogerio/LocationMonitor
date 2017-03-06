package com.romio.locationtest.net;

import com.romio.locationtest.net.in.GeneralResponse;
import com.romio.locationtest.net.in.ZoneEntity;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by roman on 3/5/17
 */

public interface KolejkaAPI {

    @POST("zones")
    ZoneEntity addZone(@Body ZoneEntity zone);

    @GET("zones")
    GeneralResponse<List<ZoneEntity>> getZones();

    @DELETE("zones/{zone_id}")
    GeneralResponse<Object> deleteZone(@Field("zone_id") String zoneId);
}
