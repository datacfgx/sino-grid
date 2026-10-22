package br.eni.cora;

import android.content.*;

public class Prefs {
    static SharedPreferences p(Context c) { return c.getSharedPreferences("sino", Context.MODE_PRIVATE); }
    public static String host(Context c) { return p(c).getString("host", "sino-relay.worldeffects752.workers.dev"); }
    public static String id(Context c) { return p(c).getString("id", "g52"); }
    static void set(Context c, String h, String i) { p(c).edit().putString("host", h).putString("id", i).apply(); }
}
