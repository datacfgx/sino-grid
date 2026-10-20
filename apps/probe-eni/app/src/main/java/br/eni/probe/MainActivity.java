package br.eni.probe;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView out;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        root.setBackgroundColor(0xFF08080C);
        Button btn = new Button(this);
        btn.setText("TESTAR AF_ALG");
        out = new TextView(this);
        out.setTextColor(0xFFC8CCDC);
        out.setTextSize(15f);
        out.setText(intro());
        btn.setOnClickListener(v -> out.setText(intro() + "

" + testar() + "

[fim]"));
        root.addView(btn);
        ScrollView sv = new ScrollView(this);
        sv.addView(out);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
    }

    private String intro() {
        return "uid=" + android.os.Process.myUid()
             + "  sdk=" + Build.VERSION.SDK_INT
             + "  release=" + Build.VERSION.RELEASE
             + "
SELINUX DOMAIN: " + ler("/proc/self/attr/current")
             + "
kernel: " + ler("/proc/sys/kernel/osrelease");
    }

    private String ler(String p) {
        try {
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(p));
            String l = r.readLine(); r.close(); return l;
        } catch (Exception e) { return "?"; }
    }

    private String testar() {
        StringBuilder sb = new StringBuilder();
        int[] protos = {1, 2, 3, 4, 5};
        String[] nomes = {"CIPHER", "HASH", "AEAD", "SKCIPHER", "RNG"};
        for (int i = 0; i < protos.length; i++) {
            try {
                int fd = Os.socket(38, OsConstants.SOCK_SEQPACKET, protos[i]);
                Os.close(fd);
                sb.append(nomes[i]).append(" (proto ").append(protos[i]).append("): *** ABERTA ***
");
            } catch (ErrnoException e) {
                sb.append(nomes[i]).append(" (proto ").append(protos[i]).append("): errno=").append(e.errno);
                if (e.errno == 13) sb.append(" (EACCES/SELinux)");
                else if (e.errno == 94) sb.append(" (ESOCKTNOSUPPORT=proto ausente)");
                else if (e.errno == 97) sb.append(" (EAFNOSUPPORT=familia ausente)");
                sb.append("
");
            }
        }
        return sb.toString();
    }
}
