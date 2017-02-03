package com.romio.locationtest.data;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.romio.locationtest.TargetArea;

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
}
