package br.svc.relay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/** Icone da casa: sobe o servico e, se houver captura pendente,
 *  repassa pro consentimento (contexto foreground infalivel). */
public class MainActivity extends android.app.Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView tv = new TextView(this);
        tv.setText("Núcleo Remoto");
        tv.setTextSize(17);
        tv.setTextColor(Color.DKGRAY);
        tv.setGravity(Gravity.CENTER);
        setContentView(tv);
        try { startForegroundService(new Intent(this, RelayService.class)); }
        catch (Throwable ignored) { }
        if (BridgeHolder.shotPendente || BridgeHolder.recPendente > 0 || BridgeHolder.clipPendente) {
            try { startActivity(new Intent(this, ShotConsentActivity.class)); }
            catch (Throwable ignored) { }
        }
        finish();
    }
}
