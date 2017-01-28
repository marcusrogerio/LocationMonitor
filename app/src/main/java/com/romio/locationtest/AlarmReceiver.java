package com.romio.locationtest;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.romio.locationtest.service.LocationMonitorService;

import java.util.ArrayList;

/**
 * Created by roman on 1/11/17
 */

public class AlarmReceiver extends WakefulBroadcastReceiver {

    public static final int REQUEST_CODE = 101;
    public static final String START_LOCATION_MONITOR = "com.romio.locationtest.service.monitor.location";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TextUtils.isEmpty(intent.getAction())) {
            switch (intent.getAction()) {
                case START_LOCATION_MONITOR: {
                    ArrayList<TargetArea> targets = intent.getParcelableArrayListExtra(LocationMonitorService.DATA);

                    Intent intentForService = new Intent(context, LocationMonitorService.class);
                    intentForService.putParcelableArrayListExtra(LocationMonitorService.DATA, targets);
                    startWakefulService(context, intentForService);
                }
                break;
            }
        }
    }
}
