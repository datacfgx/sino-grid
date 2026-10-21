package br.eni.cora;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;

public class MainActivity extends Activity {
    EditText host, devid;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(32, 32, 32, 32);
        host = new EditText(this); host.setHint("host do relay (ex: sino-relay.xxx.workers.dev)");
        host.setText(Prefs.host(this));
        devid = new EditText(this); devid.setHint("id do aparelho (ex: g52)");
        devid.setText(Prefs.id(this));
        Button save = new Button(this); save.setText("SALVAR + INICIAR TUNEL");
        TextView st = new TextView(this); st.setTextSize(15f);
        save.setOnClickListener(v -> {
            Prefs.set(this, host.getText().toString().trim(), devid.getText().toString().trim());
            startService(new Intent(this, SinoService.class));
        });
        r.addView(host); r.addView(devid); r.addView(save); r.addView(st);
        setTitle("CORA 💓 — Agente ENI");
        setContentView(r);
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override public void run() { st.setText("status: " + SinoService.status); h.postDelayed(this, 2000); }
        }, 500);
        startService(new Intent(this, SinoService.class));
    }
}
