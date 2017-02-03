package com.romio.locationtest;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by roman on 1/26/17
 */

public abstract class WakeLocker {
    private static PowerManager.WakeLock wakeLock;

    public static void acquire(Context context) {
        if (wakeLock != null) wakeLock.release();

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LocationMonitorApp.TAG);
        wakeLock.acquire();
    }

    public static void release() {
        if (wakeLock != null) wakeLock.release(); wakeLock = null;
    }
}