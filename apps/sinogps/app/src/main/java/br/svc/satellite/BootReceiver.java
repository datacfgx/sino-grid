package br.svc.satellite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        String a = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_BOOT_COMPLETED.equals(a)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)
                || "br.svc.satellite.ARM".equals(a)) {
            Intent i = new Intent(ctx, SinoGpsService.class);
            ctx.startForegroundService(i);
        }
    }
}
