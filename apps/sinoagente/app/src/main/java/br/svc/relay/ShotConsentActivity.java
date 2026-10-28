package br.svc.relay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

/** Consentimento da captura de tela (1x por boot). Systema exige toque humano. */
public class ShotConsentActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mpm.createScreenCaptureIntent(), 1001);
    }

    @Override
    protected void onActivityResult(int req, int resultCode, Intent data) {
        super.onActivityResult(req, resultCode, data);
        if (req == 1001 && resultCode == RESULT_OK && data != null) {
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            MediaProjection mp = mpm.getMediaProjection(resultCode, data);
            mp.registerCallback(new MediaProjection.Callback() {
                @Override public void onStop() { BridgeHolder.projection = null; }
            }, null);
            BridgeHolder.projection = mp;
            if (BridgeHolder.tg != null) BridgeHolder.tg.say("shot: captura autorizada!");
            if (BridgeHolder.shotPendente) {
                BridgeHolder.shotPendente = false;
                RelayService.dispararCaptura();
            }
        } else if (BridgeHolder.tg != null) {
            BridgeHolder.tg.say("shot: consentimento negado - tente de novo com 'shot'");
        }
        finish();
    }
}
