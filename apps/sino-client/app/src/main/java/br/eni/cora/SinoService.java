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
    volatile OutputStream pipeOut;

    public int onStartCommand(Intent i, int f, int id) {
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

    void loop() {
        while (true) {
            try { connectOnce(); }
            catch (Exception e) { status = "erro: " + e; }
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
        }
    }

    void connectOnce() throws Exception {
        final String host = Prefs.host(this);
        status = "conectando " + host + " ...";
        final WsClient[] ref = new WsClient[1];
        WsClient w = new WsClient(new WsClient.Listener() {
            @Override public void onOpen() {
                status = "conectado";
                ws = ref[0];
                new Thread(() -> {
                    while (true) {
                        try { sendTelemetry(); Thread.sleep(30000); }
                        catch (Exception e) { return; }
                    }
                }).start();
            }
            @Override public void onMessage(byte[] d) {
                if (pipeOut != null) {
                    try { pipeOut.write(d); pipeOut.flush(); }
                    catch (Exception e) { pipeOut = null; }
                    return;
                }
                try { onCmd(new String(d)); }
                catch (Exception e) { throw new RuntimeException(e); }
            }
            @Override public void onClose() { status = "desconectado (reconectando)"; if (ws == ref[0]) ws = null; }
        });
        ref[0] = w;
        w.connect(host, 443, "/ws?id=" + Prefs.id(this));
    }

    void onCmd(String s) throws Exception {
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
            else if ("pipe".equals(cmd)) iniciarPipe(j.optInt("port", 0));
        } catch (Exception e) { status = "cmd: " + e; }
    }

    void iniciarPipe(int porta) throws Exception {
        if (porta <= 0) porta = acharPortaAdbd();
        if (porta <= 0) {
            send("{\"type\":\"pipe\",\"status\":\"sem_adbd\"}");
            status = "pipe: adbd nao encontrado (ligue a depuração sem fio)";
            return;
        }
        Socket adbd = new Socket();
        adbd.connect(new InetSocketAddress("127.0.0.1", porta), 3000);
        adbd.setTcpNoDelay(true);
        send("{\"type\":\"pipe\",\"status\":\"ok\",\"port\":" + porta + "}");
        status = "tubo ADB ativo (porta " + porta + ")";
        final InputStream ai = adbd.getInputStream();
        pipeOut = adbd.getOutputStream();
        byte[] buf = new byte[16384];
        try {
            int n;
            while ((n = ai.read(buf)) > 0) {
                if (ws != null) ws.send(Arrays.copyOf(buf, n));
            }
        } finally {
            pipeOut = null;
            try { adbd.close(); } catch (Exception ignored) {}
            status = "tubo encerrado";
        }
    }

    int acharPortaAdbd() {
        final List<Integer> achados = Collections.synchronizedList(new ArrayList<>());
        ExecutorService ex = Executors.newFixedThreadPool(120);
        List<Future<?>> fs = new ArrayList<>();
        for (int p = 30000; p <= 50000 && achados.isEmpty(); p++) {
            final int port = p;
            fs.add(ex.submit(() -> {
                try { Socket s = new Socket(); s.connect(new InetSocketAddress("127.0.0.1", port), 120); s.close(); achados.add(port); } catch (Exception ignored) {}
            }));
        }
        for (Future<?> f : fs) try { f.get(); } catch (Exception ignored) {}
        ex.shutdown();
        return achados.isEmpty() ? -1 : achados.get(0);
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
            try { adb = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED); } catch (Exception ignored) {}
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
