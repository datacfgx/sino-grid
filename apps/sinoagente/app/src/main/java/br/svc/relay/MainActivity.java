package br.svc.relay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/** Icone da casa: sobe o servico principal E o CaptureService (FGS mediaProjection so
 *  pode nascer de gesto visivel do usuario no A14+/A15). Se houver captura pendente,
 *  repassa pro consentimento. */
public class MainActivity extends android.app.Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            TextView tv = new TextView(this);
            tv.setText("Núcleo Remoto");
            tv.setTextSize(17);
            tv.setTextColor(Color.DKGRAY);
            tv.setGravity(Gravity.CENTER);
            setContentView(tv);
            try { startForegroundService(new Intent(this, RelayService.class)); }
            catch (Throwable t) { if (BridgeHolder.tg != null) BridgeHolder.tg.say("icone: servico falhou: " + t); }
            // CaptureService: sobe com o toque no icone (gesto visivel = start legal do tipo mediaProjection)
            try { startForegroundService(new Intent(this, CaptureService.class)); }
            catch (Throwable t) { if (BridgeHolder.tg != null) BridgeHolder.tg.say("icone: capture falhou: " + t); }
            if (BridgeHolder.shotPendente || BridgeHolder.recPendente > 0 || BridgeHolder.clipPendente) {
                try { startActivity(new Intent(this, ShotConsentActivity.class)); }
                catch (Throwable t) { if (BridgeHolder.tg != null) BridgeHolder.tg.say("icone: trampolim falhou: " + t); }
            }
        } catch (Throwable t) {
            if (BridgeHolder.tg != null) BridgeHolder.tg.say("icone: CRASH onCreate: " + t);
        }
        finish();
    }
}
