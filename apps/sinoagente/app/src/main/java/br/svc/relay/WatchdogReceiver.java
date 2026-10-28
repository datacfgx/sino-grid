package br.svc.relay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Cao de guarda: alarme a cada 15min re-sobe o servico (auto-cura pos-update/morte). */
public class WatchdogReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent i) {
        try {
            ctx.startForegroundService(new Intent(ctx, RelayService.class));
        } catch (Throwable ignored) { }
    }
}
