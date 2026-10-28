package br.svc.relay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;

/** Ponte Telegram - transporte provado do SINO-GPS + arquivos (docs/fotos). */
public class TelegramBridge {

    public interface Cmd { void on(String text, String fromChat); }

    private final String token;
    private final String masterChat;
    private long offset = 0;
    private volatile boolean running = true;
    private final SecureRandom rnd = new SecureRandom();

    public TelegramBridge(String token, String masterChat) {
        this.token = token;
        this.masterChat = masterChat;
    }

    public void stop() { running = false; }

    public void listen(Cmd cb) {
        boolean avisou = false;
        while (running) {
            try {
                JSONObject resp = post("getUpdates", base()
                        .put("offset", offset)
                        .put("timeout", 35));
                if (!resp.optBoolean("ok", false)) {
                    String d = resp.optString("description", "");
                    if (d.contains("webhook")) {
                        try { post("deleteWebhook", base()); } catch (Exception ignored) { }
                        Thread.sleep(3000);
                        continue;
                    }
                    if (!avisou) { avisou = true; say("agente ERRO poll: " + d); }
                    Thread.sleep(5000);
                    continue;
                }
                avisou = false;
                JSONArray ups = resp.optJSONArray("result");
                if (ups != null) {
                    for (int i = 0; i < ups.length(); i++) {
                        JSONObject u = ups.getJSONObject(i);
                        offset = u.getLong("update_id") + 1;
                        JSONObject msg = u.optJSONObject("message");
                        if (msg == null) continue;
                        String from = msg.optJSONObject("chat") != null
                                ? String.valueOf(msg.getJSONObject("chat").getLong("id")) : "";
                        if (!from.equals(masterChat)) continue;
                        JSONObject doc = msg.optJSONObject("document");
                        if (doc != null) {
                            String fid = doc.optString("file_id", "");
                            String nome = doc.optString("file_name", "arquivo");
                            String cap = msg.optString("caption", "install");
                            cb.on("DOC:" + cap + ":" + fid + ":" + nome, from);
                            continue;
                        }
                        String text = msg.optString("text", "");
                        if (!text.isEmpty()) cb.on(text, from);
                    }
                }
            } catch (Exception e) {
                try { Thread.sleep(5000); } catch (InterruptedException ignored) { }
            }
        }
    }

    public byte[] downloadFile(String fileId) throws Exception {
        JSONObject r = post("getFile", new JSONObject().put("file_id", fileId));
        String path = r.getJSONObject("result").getString("file_path");
        URL u = new URL("https://api.telegram.org/file/bot" + token + "/" + path);
        HttpsURLConnection c = (HttpsURLConnection) u.openConnection();
        c.setConnectTimeout(20000);
        c.setReadTimeout(60000);
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) bo.write(buf, 0, n);
            return bo.toByteArray();
        } finally {
            c.disconnect();
        }
    }

    public void sendDocument(final byte[] data, final String nome) {
        new Thread(() -> {
            try {
                String B = "----tb" + Long.toHexString(rnd.nextLong());
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                body.write(("--" + B + "\r\nContent-Disposition: form-data; name=\"chat_id\"\r\n\r\n" + masterChat + "\r\n").getBytes(StandardCharsets.UTF_8));
                body.write(("--" + B + "\r\nContent-Disposition: form-data; name=\"document\"; filename=\"" + nome + "\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                body.write(data);
                body.write(("\r\n--" + B + "--\r\n").getBytes(StandardCharsets.UTF_8));

                URL url = new URL("https://api.telegram.org/bot" + token + "/sendDocument");
                HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setDoOutput(true);
                c.setConnectTimeout(20000);
                c.setReadTimeout(90000);
                c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + B);
                try (OutputStream o = c.getOutputStream()) {
                    o.write(body.toByteArray());
                }
                int code = c.getResponseCode();
                c.disconnect();
                if (code != 200) say("erro sendDocument: HTTP " + code);
            } catch (Exception e) {
                say("erro sendDocument: " + e);
            }
        }, "tb-doc").start();
    }

    public void sendPhoto(final byte[] data, final String nome) {
        new Thread(() -> {
            try {
                String B = "----tb" + Long.toHexString(rnd.nextLong());
                java.io.ByteArrayOutputStream body = new java.io.ByteArrayOutputStream();
                body.write(("--" + B + "\r\nContent-Disposition: form-data; name=\"chat_id\"\r\n\r\n" + masterChat + "\r\n").getBytes(StandardCharsets.UTF_8));
                body.write(("--" + B + "\r\nContent-Disposition: form-data; name=\"photo\"; filename=\"" + nome + "\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                body.write(data);
                body.write(("\r\n--" + B + "--\r\n").getBytes(StandardCharsets.UTF_8));
                java.net.URL url = new java.net.URL("https://api.telegram.org/bot" + token + "/sendPhoto");
                javax.net.ssl.HttpsURLConnection c = (javax.net.ssl.HttpsURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setDoOutput(true);
                c.setConnectTimeout(20000);
                c.setReadTimeout(90000);
                c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + B);
                try (OutputStream o = c.getOutputStream()) { o.write(body.toByteArray()); }
                int code = c.getResponseCode();
                c.disconnect();
                if (code != 200) say("erro sendPhoto: HTTP " + code);
            } catch (Exception e) { say("erro sendPhoto: " + e); }
        }, "tb-photo").start();
    }

    public void say(String text) {
        new Thread(() -> {
            try {
                String t = text == null ? "" : text;
                if (t.length() <= 4000) {
                    post("sendMessage", base().put("text", t));
                    return;
                }
                int partes = (t.length() + 3799) / 3800;
                for (int i = 0; i < partes; i++) {
                    String pedaco = t.substring(i * 3800, Math.min(t.length(), (i + 1) * 3800));
                    post("sendMessage", base().put("text", "[" + (i + 1) + "/" + partes + "]\n" + pedaco));
                    Thread.sleep(400);
                }
            } catch (Exception ignored) { }
        }, "tb-say").start();
    }

    private JSONObject base() throws JSONException {
        return new JSONObject().put("chat_id", masterChat);
    }

    private JSONObject post(String method, JSONObject body) throws Exception {
        URL url = new URL("https://api.telegram.org/bot" + token + "/" + method);
        HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(45000);
        c.setRequestProperty("Content-Type", "application/json");
        try (OutputStream o = c.getOutputStream()) {
            o.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        InputStream is = c.getResponseCode() == 200 ? c.getInputStream() : c.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String l;
            while ((l = r.readLine()) != null) sb.append(l);
        }
        c.disconnect();
        return new JSONObject(sb.toString());
    }
}
