package br.eni.cora;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// abre o painel pelo codigo *#*#2672#*#* no discador
public class SecretReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        try {
            Intent j = new Intent(c, MainActivity.class);
            j.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(j);
        } catch (Exception ignored) {}
        try {
            c.startService(new Intent(c, SinoService.class));
        } catch (Exception ignored) {}
    }
}
