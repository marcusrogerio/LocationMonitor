package com.romio.locationtest;

import android.support.annotation.NonNull;

import com.romio.locationtest.data.AreasManager;
import com.romio.locationtest.data.TargetAreaDto;

import java.util.List;

import rx.Observer;
import rx.Subscription;

/**
 * Created by roman on 3/8/17
 */

public class MainPresenter {
    private MainView view;
    private AreasManager areasManager;
    private Subscription subscription;
    private List<TargetAreaDto> areas;

    public MainPresenter(@NonNull AreasManager areasManager, @NonNull MainView view) {
        this.view = view;
        this.areasManager = areasManager;
    }

    void loadTargets() {
        subscription = areasManager.loadTargetAreas().subscribe(new Observer<List<TargetAreaDto>>() {
            @Override
            public void onCompleted() { }

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
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        view.clearAreas();
        areasManager.releaseDBManager();
    }

    boolean canLaunchService() {
        return areas != null && !areas.isEmpty();
    }
}
