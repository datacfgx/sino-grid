package br.svc.relay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/** FGS do tipo mediaProjection - exigido pelo Android 14 antes de getMediaProjection(). */
public class CaptureService extends Service {
    public static volatile boolean noAr = false;

    @Override
    public void onCreate() {
        super.onCreate();
        noAr = false;
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel("sync_data_min",
                    "sincronizacao", NotificationManager.IMPORTANCE_MIN));
        }
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, "sync_data_min")
                : new Notification.Builder(this);
        b.setContentTitle("Núcleo Remoto")
         .setContentText("Ativo…")
         .setSmallIcon(android.R.drawable.stat_notify_sync)
         .setOngoing(false).setShowWhen(false);
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(3003, b.build(),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else {
            startForeground(3003, b.build());
        }
        noAr = true;
    }

    @Override
    public void onDestroy() {
        noAr = false;
        super.onDestroy();
    }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }
    @Override public IBinder onBind(Intent i) { return null; }
}
