package br.svc.relay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Auto-arm: boot, update (MY_PACKAGE_REPLACED) ou ARM externo. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        String a = intent != null ? intent.getAction() : "";
        if (Intent.ACTION_BOOT_COMPLETED.equals(a)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)
                || "br.svc.relay.ARM".equals(a)) {
            try {
                ctx.getSharedPreferences("ag", Context.MODE_PRIVATE).edit().remove("anunciado").apply();
            } catch (Throwable ignored) { }
            try {
                ctx.startForegroundService(new Intent(ctx, RelayService.class));
            } catch (Throwable ignored) { }
        }
    }
}
