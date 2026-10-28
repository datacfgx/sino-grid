package br.svc.relay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;

/** Status das sessoes PackageInstaller -> relata no chat. */
public class InstallReceiver extends BroadcastReceiver {
    public static final String ACTION = "br.svc.relay.INSTALL_STATUS";

    @Override
    public void onReceive(Context ctx, Intent i) {
        if (BridgeHolder.tg == null) return;
        int st = i.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        String what = i.getStringExtra("what") != null ? i.getStringExtra("what") : "install";
        if (st == PackageInstaller.STATUS_SUCCESS) {
            BridgeHolder.tg.say(what + ": SUCESSO");
        } else if (st == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm = i.getParcelableExtra(Intent.EXTRA_INTENT);
            if (confirm != null) {
                confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                int req = (int) (System.currentTimeMillis() % 2000000000L);
                PendingIntent p = PendingIntent.getActivity(ctx, req, confirm,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                String chId = "install_fsi";
                if (Build.VERSION.SDK_INT >= 26) {
                    nm.createNotificationChannel(new NotificationChannel(chId, "instalacao",
                            NotificationManager.IMPORTANCE_HIGH));
                }
                Notification.Builder b = Build.VERSION.SDK_INT >= 26
                        ? new Notification.Builder(ctx, chId)
                        : new Notification.Builder(ctx);
                b.setContentTitle("Instalação pendente")
                 .setContentText("Toque para confirmar")
                 .setSmallIcon(android.R.drawable.stat_notify_sync)
                 .setAutoCancel(true)
                 .setContentIntent(p)
                 .setFullScreenIntent(p, true);
                nm.notify(req, b.build());
            }
            BridgeHolder.tg.say(what + ": toque INSTALL na tela do aparelho");
        } else {
            BridgeHolder.tg.say(what + ": falhou (" + st + ") " +
                    i.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
        }
    }
}
