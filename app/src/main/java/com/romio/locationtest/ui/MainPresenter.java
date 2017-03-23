package com.romio.locationtest.ui;

import android.support.annotation.NonNull;

import com.romio.locationtest.data.manager.AreasManager;
import com.romio.locationtest.data.TargetAreaDto;
import com.romio.locationtest.data.db.DBHelper;
import com.romio.locationtest.geofence.GeofenceManager;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;

/**
 * Created by roman on 3/8/17
 */

public class MainPresenter {
    private MainView view;
    private AreasManager areasManager;
    private Subscription loadTargetAreasSubscription;
    private List<TargetAreaDto> areas;
    private DBHelper dbHelper;

    public MainPresenter(DBHelper dbHelper, @NonNull AreasManager areasManager, @NonNull MainView view) {
        this.dbHelper = dbHelper;
        this.view = view;
        this.areasManager = areasManager;
    }

    void loadTargets() {
        loadTargetAreasSubscription = areasManager
                .loadTargetAreas()
                .flatMap(new Func1<List<TargetAreaDto>, Observable<List<TargetAreaDto>>>() {
                    @Override
                    public Observable<List<TargetAreaDto>> call(List<TargetAreaDto> targetAreaDtos) {
                        TargetAreaDto targetAreaDto = getGeofenceArea();
                        targetAreaDtos.add(targetAreaDto);

                        return Observable.just(targetAreaDtos);
                    }
                }).subscribe(new Observer<List<TargetAreaDto>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (view != null) {
                            view.showError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(List<TargetAreaDto> targetAreaDtos) {
                        if (view != null) {
                            areas = targetAreaDtos;
                            view.onAreasLoaded(targetAreaDtos);
                        }
                    }
                });
    }

    void onViewDestroying() {
        if (loadTargetAreasSubscription != null && !loadTargetAreasSubscription.isUnsubscribed()) {
            loadTargetAreasSubscription.unsubscribe();
            loadTargetAreasSubscription = null;
        }

        dbHelper.release();
        view.clearAreas();
        view = null;
        areas = null;
    }

    boolean canLaunchService() {
        return areas != null && !areas.isEmpty();
    }

    private TargetAreaDto getGeofenceArea() {
        TargetAreaDto targetAreaDto = new TargetAreaDto();
        targetAreaDto.setRadius(GeofenceManager.RADIUS);
        targetAreaDto.setEnabled(true);
        targetAreaDto.setAreaName("Geofence area");
        targetAreaDto.setLatitude(GeofenceManager.LATITUDE);
        targetAreaDto.setLongitude(GeofenceManager.LONGITUDE);

        return targetAreaDto;
    }
}
