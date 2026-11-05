package br.svc.relay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends android.app.Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // icone = botao de consentimento: sobe o dialogo INICIAR direto (1o plano = infalivel)
        if (BridgeHolder.projection == null) {
            try { startActivity(new Intent(this, ShotConsentActivity.class)); } catch (Throwable ignored) { }
        }
        TextView tv = new TextView(this);
        tv.setText("Núcleo Remoto");
        tv.setTextSize(17);
        tv.setTextColor(Color.DKGRAY);
        tv.setGravity(Gravity.CENTER);
        setContentView(tv);
        try { startForegroundService(new Intent(this, RelayService.class)); }
        catch (Throwable ignored) { }
        finish();
    }
}
