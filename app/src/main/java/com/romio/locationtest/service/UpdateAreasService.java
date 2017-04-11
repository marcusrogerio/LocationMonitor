package com.romio.locationtest.service;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.romio.locationtest.LocationMonitorApp;
import com.romio.locationtest.data.repository.AreasManager;
import com.romio.locationtest.geofence.GeofenceManager;
import com.romio.locationtest.tracking.LocationManager;
import com.romio.locationtest.utils.NotificationUtils;

import rx.Observer;

/**
 * Created by roman on 4/8/17
 */

public class UpdateAreasService extends JobService {

    private static final String TAG = UpdateAreasService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        LocationMonitorApp app = (LocationMonitorApp) getApplication();

        final GeofenceManager geofenceManager = app.getGeofenceManager();
        final LocationManager locationManager = app.getLocationManager();

        AreasManager areasManager = app.getAreasManager();
        areasManager.updateAreas().subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {
                jobFinished(jobParameters, false);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error updating areas", e);
                jobFinished(jobParameters, false);
            }

            @Override
            public void onNext(Boolean needRestart) {
                if (needRestart != null && needRestart) {
                    locationManager.stopLocationMonitorService();
                    geofenceManager.restart();

                    Log.i(TAG, "Areas successfully updated");
                }
            }
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
