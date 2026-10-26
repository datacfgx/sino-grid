package br.svc.satellite;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

/**
 * Ponte Telegram: long-polling de comandos + entrega de localizacao.
 * Framework puro (org.json e HttpsURLConnection sao do proprio Android).
 */
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

    /** Loop de long-polling. Bloqueia a thread chamadora ate stop(). */
    public void listen(Cmd cb) {
        while (running) {
            try {
                JSONObject body = base()
                        .put("offset", offset)
                        .put("timeout", 35)
                        .put("allowed_updates", new JSONArray().put("message"));
                JSONObject resp = post("getUpdates", body);
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
                        if (from.equals(masterChat) && text.startsWith("/")) cb.on(text, from);
                    }
                }
            } catch (Exception e) {
                sleep(5000); // rede falhou, respira e volta
            }
        }
    }

    /** Abre sessao de localizacao ao vivo. Retorna message_id do pin. */
    public long liveLocation(double lat, double lon, int livePeriodSec) throws Exception {
        JSONObject body = base()
                .put("latitude", lat)
                .put("longitude", lon)
                .put("live_period", livePeriodSec);
        return post("sendLocation", body).getJSONObject("result").getLong("message_id");
    }

    /** Move o pin ao vivo. */
    public void moveLive(long msgId, double lat, double lon) throws Exception {
        JSONObject body = base()
                .put("message_id", msgId)
                .put("latitude", lat)
                .put("longitude", lon);
        post("editMessageLiveLocation", body);
    }

    /** Encerra o pin ao vivo (vira localizacao fixa no chat). */
    public void stopLive(long msgId) throws Exception {
        JSONObject body = base().put("message_id", msgId);
        post("stopMessageLiveLocation", body);
    }

    /** Texto simples pro chat do dono. */
    public void say(String text) {
        try {
            post("sendMessage", base().put("text", text));
        } catch (Exception ignored) { }
    }

    private JSONObject base() throws JSONException {
        return new JSONObject().put("chat_id", masterChat);
    }

    private JSONObject post(String method, JSONObject body) throws Exception {
        URL url = new URL("https://api.telegram.org/bot" + token + "/" + method);
        HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(15_000);
        c.setReadTimeout(40_000);
        c.setRequestProperty("Content-Type", "application/json");
        try (OutputStream o = c.getOutputStream()) {
            o.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        InputStream is = c.getResponseCode() == 200 ? c.getInputStream() : c.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String l; while ((l = r.readLine()) != null) sb.append(l);
        }
        c.disconnect();
        return new JSONObject(sb.toString());
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }
}
