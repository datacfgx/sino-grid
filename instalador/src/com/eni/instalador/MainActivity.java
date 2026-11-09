package com.eni.instalador;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends Activity {
    TextView status;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 60, 40, 40);
        status = new TextView(this);
        status.setText("ENI INSTALADOR XAPK\n\nAbra um arquivo .xapk com este app\n(base + splits num so pacote)");
        Button btn = new Button(this);
        btn.setText("ESCOLHER .XAPK");
        btn.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(i, 42);
        });
        box.addView(status);
        box.addView(btn);
        ScrollView sv = new ScrollView(this);
        sv.addView(box);
        setContentView(sv);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 42 && res == RESULT_OK && data != null) {
            Uri uri = data.getData();
            new Thread(() -> instalar(uri)).start();
        }
        if (req == 43 && data != null) {
            int st = data.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
            String msg = data.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            if (st == PackageInstaller.STATUS_SUCCESS) {
                mostrar("INSTALADO COM SUCESSO");
            } else {
                mostrar("FALHOU (codigo " + st + "): " + msg);
            }
        }
    }

    void mostrar(String s) {
        runOnUiThread(() -> status.append("\n" + s));
    }

    void instalar(Uri uri) {
        try {
            mostrar("Lendo o pacote...");
            File dir = new File(getCacheDir(), "xapk");
            apagar(dir);
            dir.mkdirs();
            List<File> apks = new ArrayList<>();
            ZipInputStream zin = new ZipInputStream(getContentResolver().openInputStream(uri));
            ZipEntry e;
            byte[] buf = new byte[1 << 16];
            while ((e = zin.getNextEntry()) != null) {
                String nome = e.getName();
                if (nome.endsWith(".apk") && !e.isDirectory()) {
                    File out = new File(dir, nome.replace("/", "_"));
                    FileOutputStream fos = new FileOutputStream(out);
                    int n;
                    while ((n = zin.read(buf)) > 0) {
                        fos.write(buf, 0, n);
                    }
                    fos.close();
                    apks.add(out);
                    mostrar("  extraiu: " + nome);
                }
            }
            zin.close();
            if (apks.isEmpty()) {
                mostrar("ERRO: nenhum APK dentro do .xapk");
                return;
            }
            File base = acharBase(apks);
            mostrar("Base: " + base.getName() + " | total: " + apks.size() + " APKs");

            mostrar("Abrindo sessao de instalacao...");
            PackageInstaller pi = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams sp =
                    new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int id = pi.createSession(sp);
            PackageInstaller.Session session = pi.openSession(id);

            escrever(session, base, "base.apk");
            for (File f : apks) {
                if (!f.equals(base)) {
                    escrever(session, f, "split_" + f.getName());
                }
            }

            Intent callback = new Intent(this, MainActivity.class);
            callback.setAction("com.eni.instalador.RESULT");
            PendingIntent pend = PendingIntent.getActivity(
                    this, 43, callback, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            mostrar("Confirmando no instalador do Android...");
            session.commit(pend.getIntentSender());
        } catch (Exception ex) {
            mostrar("ERRO: " + ex.getMessage());
        }
    }

    File acharBase(List<File> apks) {
        for (File f : apks) {
            String n = f.getName();
            if (!n.contains("config.") && !n.startsWith("split_")) return f;
        }
        return apks.get(0);
    }

    void escrever(PackageInstaller.Session session, File apk, String nome) throws Exception {
        long tamanho = apk.length();
        OutputStream os = session.openWrite(nome, 0, tamanho);
        FileInputStreamCompat fis = new FileInputStreamCompat(apk);
        byte[] buf = new byte[1 << 16];
        long enviado = 0;
        int n;
        while ((n = fis.read(buf)) > 0) {
            os.write(buf, 0, n);
            enviado += n;
            if (tamanho > 0 && enviado % (5 << 20) < (1 << 16)) {
                mostrar("  " + nome + ": " + (enviado * 100 / tamanho) + "%");
            }
        }
        fis.close();
        os.flush();
        session.fsync(os);
        os.close();
    }

    void apagar(File f) {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs != null) for (File c : fs) apagar(c);
        }
        f.delete();
    }

    // wrapper pra nao precisar import confuso
    static class FileInputStreamCompat {
        private final InputStream in;
        FileInputStreamCompat(File f) throws Exception {
            in = new java.io.FileInputStream(f);
        }
        int read(byte[] b) throws Exception {
            return in.read(b);
        }
        void close() throws Exception {
            in.close();
        }
    }
}
