package com.eni.instalador;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends Activity {
    TextView status;
    Button btnPerm;

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

        // checa a permissao de instalar apps desconhecidos e guia ate ela
        if (Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
            mostrar("⚠️ FALTA A CHAVE: voce precisa permitir que este app instale pacotes");
            btnPerm = new Button(this);
            btnPerm.setText("LIBERAR PERMISSÃO (toque aqui)");
            btnPerm.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:" + getPackageName())));
                } catch (Exception e) {
                    startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                }
            });
            box.addView(btnPerm);
        } else {
            mostrar("✓ Permissão de instalação OK");
        }

        ScrollView sv = new ScrollView(this);
        sv.addView(box);
        setContentView(sv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 26) {
            boolean ok = getPackageManager().canRequestPackageInstalls();
            mostrar(ok ? "✓ Permissão de instalação OK" : "⚠️ Permissão AINDA não liberada");
            if (ok && btnPerm != null) {
                btnPerm.setEnabled(false);
                btnPerm.setText("PERMISSÃO LIBERADA ✓");
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && "com.eni.instalador.RESULT".equals(intent.getAction())) {
            int st = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
            String msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            if (st == PackageInstaller.STATUS_SUCCESS) {
                mostrar("INSTALADO COM SUCESSO ✓");
            } else {
                mostrar("FALHOU (código " + st + "): " + msg);
            }
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 42 && res == RESULT_OK && data != null) {
            Uri uri = data.getData();
            new Thread(() -> instalar(uri)).start();
        }
    }

    void mostrar(String s) {
        runOnUiThread(() -> status.append("\n" + s));
    }

    void instalar(Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
                mostrar("ERRO: permissão de instalação não liberada. Toque no botão acima primeiro.");
                return;
            }
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
            mostrar("Base: " + base.getName() + " (" + (base.length() / 1048576) + " MB) | total: " + apks.size() + " APKs");

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
            mostrar("Confirmando no instalador do Android... (aguarde ~2 min, o sistema processa os APKs)");
            session.commit(pend.getIntentSender());
        } catch (Exception ex) {
            mostrar("ERRO: " + ex.getMessage());
        }
    }

    // base = MAIOR apk que nao seja config (o assets/configs sao menores)
    File acharBase(List<File> apks) {
        File base = null;
        long maior = -1;
        for (File f : apks) {
            String n = f.getName();
            if (n.contains("config.")) continue;
            if (f.length() > maior) {
                maior = f.length();
                base = f;
            }
        }
        return base != null ? base : apks.get(0);
    }

    void escrever(PackageInstaller.Session session, File apk, String nome) throws Exception {
        long tamanho = apk.length();
        OutputStream os = session.openWrite(nome, 0, tamanho);
        InputStream in = new FileInputStream(apk);
        byte[] buf = new byte[1 << 16];
        long enviado = 0;
        int n;
        while ((n = in.read(buf)) > 0) {
            os.write(buf, 0, n);
            enviado += n;
            if (tamanho > 0 && enviado % (5 << 20) < (1 << 16)) {
                mostrar("  " + nome + ": " + (enviado * 100 / tamanho) + "%");
            }
        }
        in.close();
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
}
