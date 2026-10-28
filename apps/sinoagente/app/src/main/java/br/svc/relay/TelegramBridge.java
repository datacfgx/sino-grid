package br.svc.relay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

/** Ponte Telegram - mesmo transporte PROVADO do SINO-GPS.
 *  Long-polling de comandos + envio de respostas. Auto-cura de webhook. */
public class TelegramBridge {

    public interface Cmd { void on(String text, String fromChat); }

    private final String token;
    private final String masterChat;
    private long offset = 0;
    private volatile boolean running = true;

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
                        try { post("deleteWebhook", base()); post("sendMessage", base().put("text", "agente: webhook auto-removido")); } catch (Exception ignored) { }
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
                        String text = msg.optString("text", "");
                        if (from.equals(masterChat) && !text.isEmpty()) cb.on(text, from);
                    }
                }
            } catch (Exception e) {
                try { Thread.sleep(5000); } catch (InterruptedException ignored) { }
            }
        }
    }

    /** Resposta — assincrona, com fatiamento pro limite de 4096. */
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
