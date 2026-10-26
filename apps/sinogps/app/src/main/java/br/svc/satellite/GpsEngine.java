package br.svc.satellite;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

/** Motor de localizacao: LocationManager puro (GPS + rede), filtro de acuracia. */
public class GpsEngine implements LocationListener {

    public interface Fix { void onFix(Location l); }

    private final LocationManager lm;
    private Fix cb;
    private long minMs;
    private float maxAccMeters;
    private volatile Location last;

    public GpsEngine(Context ctx) {
        lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
    }

    public void start(Fix cb, long minMs, float maxAccMeters) {
        this.cb = cb;
        this.minMs = minMs;
        this.maxAccMeters = maxAccMeters;
        rearm();
        // antena fria? pede um refresh imediato da rede como ponte
        try { lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, Looper.getMainLooper()); }
        catch (SecurityException ignored) { }
    }

    public void setInterval(long ms) { this.minMs = ms; rearm(); }

    public Location last() { return last; }

    private void rearm() {
        try {
            Criteria c = new Criteria();
            c.setAccuracy(Criteria.ACCURACY_FINE);
            c.setAltitudeRequired(false);
            c.setBearingRequired(true);
            c.setSpeedRequired(true);
            lm.requestLocationUpdates(minMs, 0, c, this, Looper.getMainLooper());
        } catch (SecurityException ignored) { }
    }

    @Override
    public void onLocationChanged(Location loc) {
        last = loc;
        if (cb != null && loc.hasAccuracy() && loc.getAccuracy() <= maxAccMeters) cb.onFix(loc);
    }

    @Override public void onStatusChanged(String p, int s, Bundle b) { }
    @Override public void onProviderEnabled(String p) { }
    @Override public void onProviderDisabled(String p) { }
}
