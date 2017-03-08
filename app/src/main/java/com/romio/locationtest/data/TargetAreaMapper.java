package com.romio.locationtest.data;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.romio.locationtest.TargetArea;
import com.romio.locationtest.data.net.entity.ZoneEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 1/14/17
 */

public class TargetAreaMapper {

    @Nullable
    public static TargetArea map(@Nullable TargetAreaDto targetAreaDto) {
        if (targetAreaDto == null) {
            return null;
        }

        TargetArea targetArea = new TargetArea();
        targetArea.setAreaCenter(new LatLng(targetAreaDto.getLatitude(), targetAreaDto.getLongitude()));
        targetArea.setAreaName(targetAreaDto.getAreaName());
        targetArea.setRadius(targetAreaDto.getRadius());

        return targetArea;
    }

    public static ArrayList<TargetArea> mapFromDto(@Nullable List<TargetAreaDto> targetAreaDtoList) {
        if (targetAreaDtoList == null || targetAreaDtoList.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<TargetArea> targetAreas = new ArrayList<>();
        for (TargetAreaDto targetAreaDto : targetAreaDtoList) {
            targetAreas.add( map(targetAreaDto) );
        }

        return targetAreas;
    }

    @Nullable
    public static TargetAreaDto map(@Nullable TargetArea targetArea) {
        if (targetArea == null) {
            return null;
        }

        TargetAreaDto targetAreaDto = new TargetAreaDto();
        targetAreaDto.setLatitude(targetArea.getAreaCenter().latitude);
        targetAreaDto.setLongitude(targetArea.getAreaCenter().longitude);
        targetAreaDto.setAreaName(targetArea.getAreaName());
        targetAreaDto.setRadius(targetArea.getRadius());

        return targetAreaDto;
    }

    public static ArrayList<TargetAreaDto> mapToDto(@Nullable List<TargetArea> targetAreaList) {
        if (targetAreaList == null || targetAreaList.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<TargetAreaDto> targetAreaDtos = new ArrayList<>();
        for (TargetArea targetArea : targetAreaList) {
            targetAreaDtos.add( map(targetArea) );
        }

        return targetAreaDtos;
    }

    public static TargetAreaDto map(@Nullable ZoneEntity zoneEntity) {
        if (zoneEntity == null) {
            return null;
        }

        TargetAreaDto targetAreaDto = new TargetAreaDto();
        targetAreaDto.setAreaName(zoneEntity.getName());
        targetAreaDto.setDateCreated(zoneEntity.getDateCreated());
        targetAreaDto.setDescription(zoneEntity.getDescription());
        targetAreaDto.setEnabled(zoneEntity.isEnabled());
        targetAreaDto.setId(zoneEntity.getId());
        targetAreaDto.setLatitude(zoneEntity.getLatitude());
        targetAreaDto.setLongitude(zoneEntity.getLongitude());
        targetAreaDto.setRadius(zoneEntity.getRadius());

        return targetAreaDto;
    }

    public static List<TargetAreaDto> map(@Nullable List<ZoneEntity> zoneEntities) {
        if (zoneEntities == null || zoneEntities.isEmpty()) {
            return new ArrayList<>();
        }

        List<TargetAreaDto> targetAreaDtos = new ArrayList<>(zoneEntities.size());
        for (ZoneEntity zoneEntity : zoneEntities) {
            targetAreaDtos.add( map(zoneEntity) );
        }

        return targetAreaDtos;
    }
}
