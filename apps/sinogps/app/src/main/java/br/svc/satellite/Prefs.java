package br.svc.satellite;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String N = "sat";
    private final SharedPreferences sp;

    public Prefs(Context c) { sp = c.getSharedPreferences(N, Context.MODE_PRIVATE); }

    public long intervalMs() { return sp.getLong("interval_ms", 60_000L); }
    public void intervalMs(long v) { sp.edit().putLong("interval_ms", v).apply(); }

    public long liveMsgId() { return sp.getLong("live_msg", 0L); }
    public void liveMsgId(long v) { sp.edit().putLong("live_msg", v).apply(); }
}
