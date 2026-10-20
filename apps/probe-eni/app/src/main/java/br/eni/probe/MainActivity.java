package br.eni.probe;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.security.*;
import java.security.spec.*;

public class MainActivity extends Activity {
    static final int CNXN = 0x4e584e43, AUTH = 0x48545541, OPEN = 0x4e50454f,
                     OKAY = 0x59414b4f, CLSE = 0x45534c43, WRTE = 0x45545257;

    TextView out; EditText porta; KeyPair kp;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(0xFF08080C);

        Button bKey = new Button(this); bKey.setText("1. GERAR CHAVE ADB");
        porta = new EditText(this); porta.setHint("porta de conexao (ex 42035)");
        porta.setTextColor(0xFFFFFFFF);
        Button bShell = new Button(this); bShell.setText("2. RODAR COMO SHELL");
        out = new TextView(this); out.setTextColor(0xFFC8CCDC); out.setTextSize(13f);
        out.setText("v6 — adb client em Java\n");

        bKey.setOnClickListener(v -> gerarChave());
        bShell.setOnClickListener(v -> new Thread(this::rodarShell).start());

        root.addView(bKey); root.addView(porta); root.addView(bShell);
        ScrollView sv = new ScrollView(this); sv.addView(out);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        carregarChave();
    }

    void ui(String s) { runOnUiThread(() -> out.append(s + "\n")); }

    void carregarChave() {
        try {
            File f = new File(getFilesDir(), "kp.der");
            if (f.exists()) {
                byte[] d = Base64.decode(new String(readAll(new FileInputStream(f))), Base64.DEFAULT);
                kp = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(d)) != null
                        ? carregaPar(d) : null;
                ui("chave carregada");
            }
        } catch (Exception e) { ui("chave: " + e); }
    }

    KeyPair carregaPar(byte[] pkcs8) throws Exception {
        PrivateKey priv = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
        // re-deriva pub nao e trivial; armazenamos o par completo em outro arquivo
        File pf = new File(getFilesDir(), "pub.der");
        if (pf.exists()) {
            PublicKey pub = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(new String(readAll(new FileInputStream(pf))), Base64.DEFAULT)));
            return new KeyPair(pub, priv);
        }
        return null;
    }

    void gerarChave() {
        try {
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            kp = g.generateKeyPair();
            String b64priv = Base64.encodeToString(kp.getPrivate().getEncoded(), Base64.NO_WRAP);
            String b64pub = Base64.encodeToString(kp.getPublic().getEncoded(), Base64.NO_WRAP);
            writeAll(new FileOutputStream(new File(getFilesDir(), "kp.der")), b64priv.getBytes());
            writeAll(new FileOutputStream(new File(getFilesDir(), "pub.der")), b64pub.getBytes());
            String pem = "-----BEGIN PRIVATE KEY-----\n" + b64priv.replaceAll("(.{64})", "$1\n") + "\n-----END PRIVATE KEY-----\n";
            File dl = new File("/sdcard/Download/adbkey_eni");
            try { writeAll(new FileOutputStream(dl), pem.getBytes()); ui("chave gravada: " + dl); }
            catch (Exception e) { ui("sdcard falhou (copie da tela):\n" + pem); }
            ui("AGORA: cp /sdcard/Download/adbkey_eni ~/.android/adbkey  (no Termux, chmod 600)");
        } catch (Exception e) { ui("gerar: " + e); }
    }

    static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[4096]; int n;
        while ((n = is.read(buf)) > 0) bo.write(buf, 0, n);
        return bo.toByteArray();
    }
    static void writeAll(OutputStream os, byte[] d) throws IOException { os.write(d); os.flush(); os.close(); }

    static class Msg { int cmd, a0, a1; byte[] payload; }

    static void send(OutputStream os, int cmd, int a0, int a1, byte[] p) throws IOException {
        ByteBuffer h = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
        h.putInt(cmd); h.putInt(a0); h.putInt(a1); h.putInt(p.length);
        long sum = 0; for (byte x : p) sum += (x & 0xff);
        h.putInt((int) (sum & 0xffffffffL));
        h.putInt(cmd ^ 0xffffffff);
        os.write(h.array());
        if (p.length > 0) os.write(p);
        os.flush();
    }

    static Msg recv(InputStream is) throws IOException {
        byte[] h = new byte[24];
        int o = 0;
        while (o < 24) { int n = is.read(h, o, 24 - o); if (n < 0) throw new EOFException(); o += n; }
        ByteBuffer bb = ByteBuffer.wrap(h).order(ByteOrder.LITTLE_ENDIAN);
        Msg m = new Msg();
        m.cmd = bb.getInt(); m.a0 = bb.getInt(); m.a1 = bb.getInt();
        int len = bb.getInt();
        bb.getInt(); bb.getInt();
        m.payload = new byte[len];
        o = 0;
        while (o < len) { int n = is.read(m.payload, o, len - o); if (n < 0) throw new EOFException(); o += n; }
        return m;
    }

    void rodarShell() {
        try {
            if (kp == null) { ui("gere a chave primeiro"); return; }
            int port = Integer.parseInt(porta.getText().toString().trim());
            Socket s = new Socket();
            s.connect(new InetSocketAddress("127.0.0.1", port), 8000);
            s.setTcpNoDelay(true);
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();
            ui("conectado na porta " + port);

            send(os, CNXN, 0x01000001, 4096, "host::".getBytes());
            Msg m = recv(is);
            ui("msg1: " + tag(m.cmd));
            if (m.cmd == AUTH && m.a0 == 1) {
                Signature sig = Signature.getInstance("SHA1withRSA");
                sig.initSign(kp.getPrivate());
                sig.update(m.payload);
                send(os, AUTH, 2, 0, sig.sign());
                m = recv(is);
                ui("msg2: " + tag(m.cmd));
            }
            if (m.cmd != CNXN) { ui("AUTH falhou — a chave foi pareada? (" + tag(m.cmd) + ")"); s.close(); return; }
            ui("CNXN OK! negociado com adbd");

            int localId = 7;
            send(os, OPEN, localId, 0, "shell:id && cat /proc/self/attr/current && cat /proc/version".getBytes());
            StringBuilder sb = new StringBuilder();
            long deadline = System.currentTimeMillis() + 20000;
            while (System.currentTimeMillis() < deadline) {
                Msg w = recv(is);
                if (w.cmd == WRTE) {
                    sb.append(new String(w.payload));
                    send(os, OKAY, localId, w.a0, new byte[0]);
                } else if (w.cmd == OKAY) {
                    // ignore
                } else if (w.cmd == CLSE) {
                    send(os, CLSE, localId, w.a0, new byte[0]);
                    break;
                }
            }
            ui("--- saida do shell ---");
            ui(sb.toString().trim());
            ui("--- fim ---");
            s.close();
        } catch (Exception e) { ui("erro: " + e); }
    }

    String tag(int c) {
        String s = String.format("%08x", c);
        try {
            return "" + (char) (c & 0xff) + (char) ((c >> 8) & 0xff) + (char) ((c >> 16) & 0xff) + (char) ((c >> 24) & 0xff) + " (" + s + ")";
        } catch (Exception e) { return s; }
    }
}
