package br.svc.relay;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/** WebSocket cliente MINIMO (RFC6455) — stdlib pura.
 *  v1.1: rastreia ultimo frame recebido (lastRx) pra detectar socket zumbi. */
public class WsClient {

    public interface Msg { void onText(String t); }

    private Socket sock;
    private InputStream in;
    private OutputStream out;
    private volatile boolean open = false;
    private volatile long lastRx = 0;
    private final SecureRandom rnd = new SecureRandom();

    public boolean isOpen() { return open; }
    public long lastRx() { return lastRx; }

    /** Se nada passou pelo cano ha mais de ms, fecha pra forcar reconexao. */
    public boolean stale(long ms) {
        if (open && System.currentTimeMillis() - lastRx > ms) { close(); return true; }
        return false;
    }

    public void connect(String wsUrl, Msg cb, Runnable onOpen) throws Exception {
        URI u = new URI(wsUrl);
        String host = u.getHost();
        int port = u.getPort() > 0 ? u.getPort() : (u.getScheme().equals("wss") ? 443 : 80);
        String path = u.getRawPath() + (u.getRawQuery() != null ? "?" + u.getRawQuery() : "");

        sock = new Socket();
        sock.connect(new InetSocketAddress(host, port), 15000);
        sock.setSoTimeout(80_000);
        if ("wss".equals(u.getScheme())) {
            sock = ((javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault())
                    .createSocket(sock, host, port, true);
        }
        in = sock.getInputStream();
        out = sock.getOutputStream();

        byte[] nonce = new byte[16];
        rnd.nextBytes(nonce);
        String key = Base64.getEncoder().encodeToString(nonce);
        String req = "GET " + path + " HTTP/1.1\r\n"
                + "Host: " + host + "\r\n"
                + "Upgrade: websocket\r\nConnection: Upgrade\r\n"
                + "Sec-WebSocket-Key: " + key + "\r\n"
                + "Sec-WebSocket-Version: 13\r\n\r\n";
        out.write(req.getBytes(StandardCharsets.UTF_8));
        out.flush();

        ByteArrayOutputStream hb = new ByteArrayOutputStream();
        int b, p1 = -1, p2 = -1, p3 = -1;
        while ((b = in.read()) != -1) {
            hb.write(b);
            if (p3 == 13 && p2 == 10 && p1 == 13 && b == 10) break;
            p3 = p2; p2 = p1; p1 = b;
        }
        if (!hb.toString("UTF-8").contains(" 101")) throw new IOException("handshake falhou");
        open = true;
        lastRx = System.currentTimeMillis();
        if (onOpen != null) onOpen.run();

        try {
            while (open) {
                int fb;
                try { fb = in.read(); }
                catch (java.net.SocketTimeoutException ste) { continue; }
                if (fb < 0) break;
                lastRx = System.currentTimeMillis();
                int opcode = fb & 0x0f;
                int b2 = in.read(); if (b2 < 0) break;
                long len = b2 & 0x7f;
                if (len == 126) { len = ((long) in.read() << 8) | in.read(); }
                else if (len == 127) {
                    len = 0;
                    for (int i = 0; i < 8; i++) len = (len << 8) | in.read();
                }
                ByteArrayOutputStream payload = new ByteArrayOutputStream();
                for (long i = 0; i < len; i++) { int d = in.read(); if (d < 0) break; payload.write(d); }
                byte[] data = payload.toByteArray();

                if (opcode == 0x9) { sendFrame(0xA, data); }
                else if (opcode == 0x8) { sendFrame(0x8, new byte[0]); break; }
                else if (opcode == 0x1 && cb != null) { cb.onText(new String(data, StandardCharsets.UTF_8)); }
            }
        } finally {
            open = false;
            try { sock.close(); } catch (Exception ignored) { }
        }
    }

    public synchronized void sendText(String text) throws IOException {
        if (!open) throw new IOException("ws fechado");
        sendFrame(0x1, text.getBytes(StandardCharsets.UTF_8));
    }

    private void sendFrame(int opcode, byte[] payload) throws IOException {
        ByteArrayOutputStream f = new ByteArrayOutputStream();
        f.write(0x80 | opcode);
        int len = payload.length;
        if (len < 126) f.write(0x80 | len);
        else if (len < 65536) { f.write(0x80 | 126); f.write((len >> 8) & 0xff); f.write(len & 0xff); }
        else { f.write(0x80 | 127); for (int i = 7; i >= 0; i--) f.write((int) ((len >> (8 * i)) & 0xff)); }
        byte[] mask = new byte[4];
        rnd.nextBytes(mask);
        f.write(mask);
        for (int i = 0; i < len; i++) f.write(payload[i] ^ mask[i % 4]);
        out.write(f.toByteArray());
        out.flush();
    }

    public void close() {
        open = false;
        try { if (sock != null) sock.close(); } catch (Exception ignored) { }
    }
}
