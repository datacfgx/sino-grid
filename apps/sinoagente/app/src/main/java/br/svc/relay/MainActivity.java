package br.svc.relay;

import android.content.Intent;
import android.os.Bundle;

/** BUILD SETUP: icone visivel UMA vez pra descongelar aparelho virgem. */
public class MainActivity extends android.app.Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        try { startForegroundService(new Intent(this, RelayService.class)); }
        catch (Throwable ignored) { }
        finish();
    }
}
