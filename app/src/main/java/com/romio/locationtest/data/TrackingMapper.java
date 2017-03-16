package com.romio.locationtest.data;

import android.support.annotation.Nullable;

import com.romio.locationtest.data.net.entity.TrackingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 3/16/17
 */

public class TrackingMapper {

    public static TrackingEntity map(@Nullable TrackingDto trackingDto) {
        if (trackingDto == null) {
            return null;
        }

        TrackingEntity trackingEntity = new TrackingEntity();
        trackingEntity.setTrackingId(trackingDto.getTrackingId());
        trackingEntity.setTrackingTimestamp(trackingDto.getTrackingTimeStamp());
        trackingEntity.setDataType(trackingDto.getDataType());
        trackingEntity.setZoneId(trackingDto.getZoneId());
        trackingEntity.setLatitude(trackingDto.getLatitude());
        trackingEntity.setLongitude(trackingDto.getLongitude());

        return trackingEntity;
    }

    public static List<TrackingEntity> map(@Nullable List<TrackingDto> trackingDtoList) {
        List<TrackingEntity> trackingEntities = new ArrayList<>();
        if (trackingDtoList != null && !trackingDtoList.isEmpty()) {
            for (TrackingDto trackingDto : trackingDtoList) {
                trackingEntities.add( map(trackingDto) );
            }
        }

        return trackingEntities;
    }
}
