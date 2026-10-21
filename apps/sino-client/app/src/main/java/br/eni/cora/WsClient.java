package br.eni.cora;

import android.util.Base64;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class WsClient {
    public interface Listener {
        void onOpen();
        void onMessage(byte[] data);
        void onClose();
    }
    private final Listener l;
    private OutputStream out;
    private volatile boolean running = true;

    public WsClient(Listener l) { this.l = l; }

    public void connect(String host, int port, String path) throws Exception {
        SSLSocket sock = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
        sock.setTcpNoDelay(true);
        out = sock.getOutputStream();
        InputStream in = sock.getInputStream();
        byte[] kb = new byte[16];
        new java.security.SecureRandom().nextBytes(kb);
        String key = Base64.encodeToString(kb, Base64.NO_WRAP);
        out.write(("GET " + path + " HTTP/1.1\r\nHost: " + host + "\r\n" +
                "Upgrade: websocket\r\nConnection: Upgrade\r\n" +
                "Sec-WebSocket-Key: " + key + "\r\nSec-WebSocket-Version: 13\r\n" +
                "X-Role: phone\r\n\r\n").getBytes("ISO-8859-1"));
        out.flush();
        ByteArrayOutputStream hb = new ByteArrayOutputStream();
        int b, p1 = -1, p2 = -1, p3 = -1;
        while (true) {
            b = in.read();
            if (b < 0) throw new EOFException();
            hb.write(b);
            if (p1 == 13 && p2 == 10 && p3 == 13 && b == 10) break;
            p1 = p2; p2 = p3; p3 = b;
        }
        String hs = hb.toString("ISO-8859-1");
        if (!hs.contains(" 101")) throw new IOException("handshake: " + hs.split("\r\n")[0]);
        l.onOpen();
        byte[] mask = new byte[4];
        while (running) {
            int h1 = in.read(); if (h1 < 0) break;
            int h2 = in.read(); if (h2 < 0) break;
            int op = h1 & 15;
            long len = h2 & 127;
            if (len == 126) len = ((long) in.read() << 8) | in.read();
            else if (len == 127) { len = 0; for (int i = 0; i < 8; i++) len = (len << 8) | in.read(); }
            if ((h2 & 128) != 0) for (int i = 0; i < 4; i++) mask[i] = (byte) in.read();
            byte[] pl = new byte[(int) len];
            int o = 0;
            while (o < len) { int n = in.read(pl, o, (int) len - o); if (n < 0) throw new EOFException(); o += n; }
            if ((h2 & 128) != 0) for (int i = 0; i < pl.length; i++) pl[i] ^= mask[i & 3];
            if (op == 1 || op == 2) l.onMessage(pl);
            else if (op == 8) break;
            else if (op == 9) sendFrame(10, pl);
        }
        try { sock.close(); } catch (Exception ignored) {}
        l.onClose();
    }

    private synchronized void sendFrame(int op, byte[] pl) throws IOException {
        ByteArrayOutputStream f = new ByteArrayOutputStream();
        f.write(128 | op);
        int len = pl.length;
        if (len < 126) f.write(128 | len);
        else if (len < 65536) { f.write(254); f.write((len >> 8) & 255); f.write(len & 255); }
        else { f.write(255); for (int i = 7; i >= 0; i--) f.write((int) ((len >> (8 * i)) & 255)); }
        byte[] mask = new byte[4];
        new java.security.SecureRandom().nextBytes(mask);
        f.write(mask);
        for (int i = 0; i < len; i++) f.write(pl[i] ^ mask[i & 3]);
        out.write(f.toByteArray());
        out.flush();
    }

    public void send(String s) throws IOException { sendFrame(1, s.getBytes("UTF-8")); }
    public void send(byte[] b) throws IOException { sendFrame(2, b); }
    public void close() { running = false; }
}
