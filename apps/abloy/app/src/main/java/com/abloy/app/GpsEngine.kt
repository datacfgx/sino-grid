package com.abloy.app

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object GpsEngine {
    fun fix(ctx: Context, timeoutMs: Long = 15000): Location? {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var melhor: Location? = null
        for (p in lm.getProviders(true)) {
            try {
                val l = lm.getLastKnownLocation(p) ?: continue
                if (melhor == null || l.time > melhor.time) melhor = l
            } catch (_: SecurityException) { }
        }
        val trava = CountDownLatch(1)
        var fresco: Location? = null
        try {
            for (prov in listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)) {
                lm.requestSingleUpdate(prov, { loc ->
                    if (fresco == null) { fresco = loc; trava.countDown() }
                }, Looper.getMainLooper())
            }
        } catch (_: SecurityException) { return melhor }
        trava.await(timeoutMs, TimeUnit.MILLISECONDS)
        return fresco ?: melhor
    }

    fun mapsLink(loc: Location?): String =
        if (loc != null) "https://maps.google.com/?q=${loc.latitude},${loc.longitude} (±${"%.0f".format(loc.accuracy)}m)"
        else "sem localizacao disponivel"
}
