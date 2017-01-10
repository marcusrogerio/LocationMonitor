package com.romio.locationtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by roman on 1/11/17.
 */

public class LocationAlarmReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 101;
    public static final String ACTION = "com.romio.locationtest.service.start";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), ACTION)) {
            ArrayList<TargetArea> targets = intent.getParcelableArrayListExtra(LocationIntentService.DATA);

            Intent intentForService = new Intent(context, LocationIntentService.class);
            intentForService.putParcelableArrayListExtra(LocationIntentService.DATA, targets);
            context.startService(intentForService);
        }
    }
}
