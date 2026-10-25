package br.eni.cora;

import android.content.*;

public class Prefs {
    static SharedPreferences p(Context c) { return c.getSharedPreferences("sino", Context.MODE_PRIVATE); }
    public static String host(Context c) { return p(c).getString("host", "sino-relay.worldeffects752.workers.dev"); }
    public static String id(Context c) {
        String v = p(c).getString("id", "");
        if (v == null || v.isEmpty()) {
            v = gerarId(c);
            p(c).edit().putString("id", v).apply();
        }
        return v;
    }
    static String gerarId(Context c) {
        String modelo = (android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL).toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        if (modelo.length() > 14) modelo = modelo.substring(0, 14);
        String aid = android.provider.Settings.Secure.getString(c.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        String tail = Integer.toHexString(((aid == null ? "semid" : aid).hashCode()) & 0xffff);
        while (tail.length() < 4) tail = "0" + tail;
        return modelo + "-" + tail;
    }
    static void set(Context c, String h, String i) { p(c).edit().putString("host", h).putString("id", i).apply(); }
}
