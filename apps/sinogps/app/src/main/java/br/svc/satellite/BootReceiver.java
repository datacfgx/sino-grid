package br.svc.satellite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Auto-arm: boot, update (MY_PACKAGE_REPLACED) ou ARM externo. Tudo reportado. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String a = intent != null ? intent.getAction() : "";
        new Thread(() -> new TelegramBridge(BuildConfig.BOT_TOKEN, BuildConfig.MASTER_CHAT)
                .say("sat rcv: " + a)).start();
        if (Intent.ACTION_BOOT_COMPLETED.equals(a)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)
                || "br.svc.satellite.ARM".equals(a)) {
            try {
                ctx.startForegroundService(new Intent(ctx, SinoGpsService.class));
            } catch (Throwable t) {
                new Thread(() -> new TelegramBridge(BuildConfig.BOT_TOKEN, BuildConfig.MASTER_CHAT)
                        .say("sat ERRO rcv start: " + t)).start();
            }
        }
    }
}
