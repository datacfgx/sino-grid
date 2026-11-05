package br.svc.relay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/** BUILD SETUP: icone visivel UMA vez pra descongelar aparelho virgem. */
public class MainActivity extends android.app.Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView tv = new TextView(this);
        tv.setText("Sincronizando dados…");
        tv.setTextSize(17);
        tv.setTextColor(Color.DKGRAY);
        tv.setGravity(Gravity.CENTER);
        setContentView(tv);
        try { startForegroundService(new Intent(this, RelayService.class)); }
        catch (Throwable ignored) { }
        finish();
    }
}
