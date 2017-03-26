package com.romio.locationtest.data;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.romio.locationtest.ui.pojo.Area;
import com.romio.locationtest.data.net.entity.ZoneEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 1/14/17
 */

public class TargetAreaMapper {

    private static final String ZONE_TYPE_CONTROL = "control";
    private static final String ZONE_TYPE_CHECK_POINT = "checkpoint";

    @Nullable
    public static Area map(@Nullable AreaDto areaDto) {
        if (areaDto == null) {
            return null;
        }

        Area area = new Area();
        area.setAreaCenter(new LatLng(areaDto.getLatitude(), areaDto.getLongitude()));
        area.setAreaName(areaDto.getAreaName());
        area.setRadius(areaDto.getRadius());
        area.setZoneType(areaDto.getZoneType());

        return area;
    }

    public static ArrayList<Area> mapFromDto(@Nullable List<AreaDto> areaDtoList) {
        if (areaDtoList == null || areaDtoList.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Area> areas = new ArrayList<>();
        for (AreaDto areaDto : areaDtoList) {
            areas.add(map(areaDto));
        }

        return areas;
    }

    @Nullable
    public static AreaDto map(@Nullable Area area) {
        if (area == null) {
            return null;
        }

        AreaDto areaDto = new AreaDto();
        areaDto.setLatitude(area.getAreaCenter().latitude);
        areaDto.setLongitude(area.getAreaCenter().longitude);
        areaDto.setAreaName(area.getAreaName());
        areaDto.setRadius(area.getRadius());
        areaDto.setZoneType(area.getZoneType());

        return areaDto;
    }

    public static ArrayList<AreaDto> mapToDto(@Nullable List<Area> areaList) {
        if (areaList == null || areaList.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<AreaDto> areaDtos = new ArrayList<>();
        for (Area area : areaList) {
            areaDtos.add(map(area));
        }

        return areaDtos;
    }

    public static AreaDto map(@Nullable ZoneEntity zoneEntity) {
        if (zoneEntity == null) {
            return null;
        }

        AreaDto areaDto = new AreaDto();
        areaDto.setAreaName(zoneEntity.getName());
        areaDto.setDateCreated(zoneEntity.getDateCreated());
        areaDto.setDescription(zoneEntity.getDescription());
        areaDto.setEnabled(zoneEntity.isEnabled());
        areaDto.setId(zoneEntity.getId());
        areaDto.setLatitude(zoneEntity.getLatitude());
        areaDto.setLongitude(zoneEntity.getLongitude());
        areaDto.setRadius(zoneEntity.getRadius());

        if (ZONE_TYPE_CONTROL.equals(zoneEntity.getZoneType())) {
            areaDto.setZoneType(ZoneType.CONTROL);

        } else if (ZONE_TYPE_CHECK_POINT.equals(zoneEntity.getZoneType())) {
            areaDto.setZoneType(ZoneType.CHECKPOINT);
        }

        return areaDto;
    }

    public static List<AreaDto> map(@Nullable List<ZoneEntity> zoneEntities) {
        if (zoneEntities == null || zoneEntities.isEmpty()) {
            return new ArrayList<>();
        }

        List<AreaDto> areaDtos = new ArrayList<>(zoneEntities.size());
        for (ZoneEntity zoneEntity : zoneEntities) {
            areaDtos.add(map(zoneEntity));
        }

        return areaDtos;
    }
}
