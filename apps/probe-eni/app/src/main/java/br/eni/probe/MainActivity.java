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
import java.io.*;

public class MainActivity extends Activity {
    private TextView out;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(0xFF08080C);
        Button btnExec = new Button(this);
        btnExec.setText("1. TESTAR EXEC (binário estático)");
        Button btnAlg = new Button(this);
        btnAlg.setText("2. TESTAR AF_ALG (domínio do app)");
        out = new TextView(this);
        out.setTextColor(0xFFC8CCDC);
        out.setTextSize(13f);
        out.setText(intro());
        btnExec.setOnClickListener(v -> out.setText(intro() + "\n\n" + testarExec() + "\n\n[fim]"));
        btnAlg.setOnClickListener(v -> out.setText(intro() + "\n\n" + testarAlg() + "\n\n[fim]"));
        root.addView(btnExec);
        root.addView(btnAlg);
        ScrollView sv = new ScrollView(this);
        sv.addView(out);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
    }

    private String intro() {
        return "uid=" + android.os.Process.myUid() + " sdk=" + Build.VERSION.SDK_INT
             + " release=" + Build.VERSION.RELEASE
             + "\nDOMAIN: " + ler("/proc/self/attr/current")
             + "\nkernel: " + ler("/proc/sys/kernel/osrelease");
    }

    private String ler(String p) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(p));
            String l = r.readLine(); r.close(); return l;
        } catch (Exception e) { return "?"; }
    }

    private String testarExec() {
        StringBuilder sb = new StringBuilder();
        try {
            File bin = new File(getFilesDir(), "hello");
            if (!bin.exists()) {
                InputStream is = getAssets().open("hello");
                FileOutputStream fo = new FileOutputStream(bin);
                byte[] buf = new byte[8192]; int n;
                while ((n = is.read(buf)) > 0) fo.write(buf, 0, n);
                fo.close(); is.close();
            }
            if (!bin.setExecutable(true, true)) sb.append("setExecutable=false!\n");
            Process p = new ProcessBuilder(bin.getAbsolutePath()).redirectErrorStream(true).start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String l;
            while ((l = r.readLine()) != null) sb.append(l).append("\n");
            p.waitFor();
            sb.append("exit=").append(p.exitValue());
        } catch (Exception e) {
            sb.append("EXEC FALHOU: ").append(e);
        }
        return sb.toString();
    }

    private String testarAlg() {
        StringBuilder sb = new StringBuilder();
        int[] protos = {1, 2, 3, 4, 5};
        String[] nomes = {"CIPHER", "HASH", "AEAD", "SKCIPHER", "RNG"};
        for (int i = 0; i < protos.length; i++) {
            try {
                java.io.FileDescriptor fd = Os.socket(38, OsConstants.SOCK_SEQPACKET, protos[i]);
                Os.close(fd);
                sb.append(nomes[i]).append(": *** ABERTA ***\n");
            } catch (ErrnoException e) {
                sb.append(nomes[i]).append(": errno=").append(e.errno);
                if (e.errno == 13) sb.append(" (EACCES/SELinux)");
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
