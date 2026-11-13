package com.abloy.app

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object GpsEngine {
    private const val MIN_ACCURACY = 20f

    fun fix(ctx: Context, timeoutMs: Long = 10000): Location? {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var best: Location? = null
        for (p in lm.getProviders(true)) {
            try {
                val l = lm.getLastKnownLocation(p) ?: continue
                if (l.accuracy <= MIN_ACCURACY && (best == null || l.time > best.time)) best = l
            } catch (_: SecurityException) { }
        }
        if (best != null && System.currentTimeMillis() - best.time < 120_000) return best

        val latch = CountDownLatch(1)
        var fresh: Location? = null
        try {
            lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, { loc ->
                if (loc.accuracy <= MIN_ACCURACY || fresh == null) { fresh = loc; latch.countDown() }
            }, Looper.getMainLooper())
        } catch (_: SecurityException) { return best }
        latch.await(timeoutMs, TimeUnit.MILLISECONDS)
        return fresh ?: best
    }

    fun mapsLink(loc: Location?): String =
        if (loc != null) "https://maps.google.com/?q=${loc.latitude},${loc.longitude} (±${"%.0f".format(loc.accuracy)}m)"
        else "sem localizacao disponivel"
}
