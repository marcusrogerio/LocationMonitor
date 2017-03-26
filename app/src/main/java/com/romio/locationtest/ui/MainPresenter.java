package com.romio.locationtest.ui;

import android.support.annotation.NonNull;

import com.romio.locationtest.data.AreaDto;
import com.romio.locationtest.data.db.DBHelper;
import com.romio.locationtest.data.repository.AreasManager;

import java.util.List;

import rx.Observer;
import rx.Subscription;

/**
 * Created by roman on 3/8/17
 */

public class MainPresenter {
    private MainView view;
    private AreasManager areasManager;
    private Subscription loadTargetAreasSubscription;
    private DBHelper dbHelper;

    public MainPresenter(DBHelper dbHelper, @NonNull AreasManager areasManager, @NonNull MainView view) {
        this.dbHelper = dbHelper;
        this.view = view;
        this.areasManager = areasManager;
    }

    void loadTargets() {
        loadTargetAreasSubscription = areasManager
                .loadAllAreas()
                .subscribe(new Observer<List<AreaDto>>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        if (view != null) {
                            view.showError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(List<AreaDto> areaDtos) {
                        if (view != null) {
                            view.onAreasLoaded(areaDtos);
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
    }
}
