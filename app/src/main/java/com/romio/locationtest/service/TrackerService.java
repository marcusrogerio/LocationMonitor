package com.romio.locationtest.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import com.romio.locationtest.AlarmReceiver;
import com.romio.locationtest.MainActivity;
import com.romio.locationtest.R;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by roman on 2/8/17
 */

public class TrackerService extends Service {
    private static final String TAG = "TripTracker/Service";

    public static TrackerService service;

    private static final int MAX_NOTIFICATION_ID_NUMBER = 1000;
    private static int notificationId = 0;

    private NotificationManager notificationManager;
    private Notification notification;

    private static boolean isRunning = false;

    private LocationListener locationListener;
    private AlarmManager alarmManager;
    private PendingIntent pendingAlarm;
    private static volatile PowerManager.WakeLock wakeLock;

    final ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_LOG = 3;
    static final int MSG_LOG_RING = 4;

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TrackerService.service = this;

        showNotification();

        isRunning = true;

		/* findAndSendLocation() will callback to this */
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                notifyUser("Location: " + location.getLatitude() + " " + location.getLongitude(), "Service");
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

		/* we don't need to be exact in our frequency, try to conserve at least
         * a little battery */
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingAlarm = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(), 10000, pendingAlarm);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(locationListener);

		/* kill persistent notification */
        notificationManager.cancelAll();

        if (pendingAlarm != null) {
            alarmManager.cancel(pendingAlarm);
        }

        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /* called within wake lock from broadcast receiver, but assert that we have
     * it so we can keep it longer when we return (since the location request
     * uses a callback) and then free it when we're done running through the
     * queue */
    public void findAndSendLocation() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "location_monitor");
            wakeLock.setReferenceCounted(true);
        }

        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { return; }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private void showNotification() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new Notification(R.drawable.ic_play_24dp, "Location Monitor Started", System.currentTimeMillis());
        notification.contentIntent = contentIntent;
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(1, notification);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);

				/* respond with our log ring to show what we've been up to */
                    try {
                        Message replyMsg = Message.obtain(null, MSG_LOG_RING);
                        replyMsg.obj = mLogRing;
                        msg.replyTo.send(replyMsg);
                    } catch (RemoteException e) {
                    }

                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void notifyUser(String message, String title) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_add_location_18dp)
                        .setContentTitle(title)
                        .setLights(Color.BLUE, 500, 500)
                        .setSound(alarmSound)
                        .setContentText(message);

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());

        if (notificationId == MAX_NOTIFICATION_ID_NUMBER) {
            notificationId = 0;
        } else {
            notificationId++;
        }
    }

}
