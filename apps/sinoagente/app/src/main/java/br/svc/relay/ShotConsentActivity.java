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

/** Consentimento da captura - janela VISIVEL obrigatoria (A14+ nega projecao sem ela). */
public class ShotConsentActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView tv = new TextView(this);
        tv.setText("Autorizando captura de tela…");
        tv.setTextSize(16);
        tv.setTextColor(Color.DKGRAY);
        tv.setGravity(Gravity.CENTER);
        setContentView(tv);
        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mpm.createScreenCaptureIntent(), 1001);
    }

    @Override
    protected void onActivityResult(int req, int resultCode, Intent data) {
        super.onActivityResult(req, resultCode, data);
        finish(); // some na hora do INICIAR - o resto rola na thread de fundo
        finish(); // some na hora: o trabalho continua na thread de fundo
        if (BridgeHolder.tg == null) { finish(); return; }
        if (req != 1001) { finish(); return; }
        new Thread(() -> {
            android.os.Looper.prepare(); // getMediaProjection cria Handler interno: exige Looper
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
                    if (mp == null && ultima != null) throw ultima;
                    if (mp == null) {
                        BridgeHolder.tg.say("shot: sistema NEGOU a projecao (nulo) - tela visivel nao bastou");
                    } else {
                        mp.registerCallback(new MediaProjection.Callback() {
                            @Override public void onStop() {
                                BridgeHolder.projection = null;
                                if (BridgeHolder.tg != null)
                                    BridgeHolder.tg.say("projecao: sessao ENCERRADA PELO SISTEMA");
                            }
                        }, null);
                        BridgeHolder.projection = mp;
                        // SENTINELA: VD 64x64 drenando - sessao nunca ociosa
                        // (Android 15 derruba projecao parada; custa zero blindar tudo)
                        try {
                            android.media.ImageReader sr = android.media.ImageReader.newInstance(64, 64, android.graphics.PixelFormat.RGBA_8888, 2);
                            android.hardware.display.VirtualDisplay sv = mp.createVirtualDisplay(
                                    "sentinela", 64, 64, 100,
                                    android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
                                    sr.getSurface(), null, null);
                            sr.setOnImageAvailableListener(r -> {
                                android.media.Image img = r.acquireLatestImage();
                                if (img != null) img.close();
                            }, new android.os.Handler(android.os.Looper.getMainLooper()));
                            BridgeHolder.sentinela = sv;
                            BridgeHolder.sentinelaReader = sr;
                        } catch (Throwable ignored) { }
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
