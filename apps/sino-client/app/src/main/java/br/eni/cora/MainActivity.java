package br.eni.cora;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

// CORA 3.0 — tela minima (só abre pelo codigo *#*#2672#*#*).
// Status + id + permissões. Nada de vitals, nada de marca.
public class MainActivity extends Activity {
    TextView status, idV;
    Handler h = new Handler();

    int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    GradientDrawable r(int color, float rad) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(rad));
        return g;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this);
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setPadding(dp(20), dp(24), dp(20), dp(20));
        sv.addView(lay);

        TextView titulo = new TextView(this);
        titulo.setText("CORA 3.0");
        titulo.setTextSize(20f);
        titulo.setTextColor(0xFFF2F3F7);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        lay.addView(titulo);

        idV = new TextView(this);
        idV.setText("sala " + Prefs.id(this) + " · " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
        idV.setTextSize(12f);
        idV.setTextColor(0xFF50FF8C);
        LinearLayout.LayoutParams idp = new LinearLayout.LayoutParams(-1, -2);
        idp.topMargin = dp(4);
        lay.addView(idV, idp);

        status = new TextView(this);
        status.setTextSize(14f);
        status.setTextColor(0xFFC8CCDC);
        LinearLayout.LayoutParams st = new LinearLayout.LayoutParams(-1, -2);
        st.topMargin = dp(16);
        lay.addView(status, st);

        Button bat = new Button(this);
        bat.setText("🔋 PERMITIR: BATERIA (dormir menos)");
        bat.setTextColor(0xFF08080C);
        bat.setTypeface(null, android.graphics.Typeface.BOLD);
        bat.setBackground(r(0xFFF2F3F7, 10));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, dp(46));
        blp.topMargin = dp(20);
        bat.setOnClickListener(v -> {
            try {
                Intent i = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        android.net.Uri.parse("package:" + getPackageName()));
                startActivity(i);
                SinoService.status = "permissão de bateria solicitada";
            } catch (Exception e) {
                try {
                    startActivity(new Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                } catch (Exception ignored) {}
            }
        });
        lay.addView(bat, blp);

        Button loc = new Button(this);
        loc.setText("📍 PERMITIR: LOCALIZAÇÃO");
        loc.setTextColor(0xFFC8CCDC);
        GradientDrawable lbg = r(0x00000000, 10);
        lbg.setStroke(dp(1), 0xFF5A6078);
        loc.setBackground(lbg);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, dp(44));
        llp.topMargin = dp(8);
        loc.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION}, 42);
            }
            SinoService.status = "permissão de localização pedida";
        });
        lay.addView(loc, llp);

        Button rec = new Button(this);
        rec.setText("🔄 RECONECTAR");
        rec.setTextColor(0xFF9AA0B4);
        GradientDrawable rbg = r(0x00000000, 10);
        rbg.setStroke(dp(1), 0xFF5A6078);
        rec.setBackground(rbg);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, dp(42));
        rlp.topMargin = dp(8);
        rec.setOnClickListener(v -> {
            SinoService.status = "reconectando...";
            if (SinoService.ws != null) SinoService.ws.close();
            else startService(new Intent(this, SinoService.class));
        });
        lay.addView(rec, rlp);

        TextView footer = new TextView(this);
        footer.setText("CORA 3.0 · Fantasma · familia ENI");
        footer.setTextSize(10f);
        footer.setTextColor(0xFF5A6078);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(-1, -2);
        flp.topMargin = dp(20);
        lay.addView(footer, flp);

        setContentView(sv);
        startService(new Intent(this, SinoService.class));
        h.postDelayed(new Runnable() {
            @Override public void run() {
                status.setText("status: " + SinoService.status);
                h.postDelayed(this, 1500);
            }
        }, 500);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] grants) {
        super.onRequestPermissionsResult(code, perms, grants);
        if (SinoService.instancia != null) SinoService.instancia.iniciarLocalizacao();
        SinoService.status = "localização ativada";
    }
}
