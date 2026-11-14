package com.abloy.app

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    const val BUILD = "r31"
    @Volatile private var migrado = false

    private fun sp(ctx: Context): SharedPreferences {
        val c = if (android.os.Build.VERSION.SDK_INT >= 24) ctx.createDeviceProtectedStorageContext() else ctx
        val novo = c.getSharedPreferences("abloy", Context.MODE_PRIVATE)
        if (!migrado) {
            migrado = true
            try {
                val velho = ctx.getSharedPreferences("abloy", Context.MODE_PRIVATE)
                if (novo.getString("token", "")!!.isBlank() && velho.getString("token", "")!!.isNotBlank()) {
                    novo.edit()
                        .putString("token", velho.getString("token", ""))
                        .putString("chat", velho.getString("chat", ""))
                        .putString("pin", velho.getString("pin", ""))
                        .putString("trusted", velho.getString("trusted", ""))
                        .putBoolean("armed", velho.getBoolean("armed", false))
                        .apply()
                }
            } catch (_: Exception) { }
        }
        return novo
    }

    fun token(c: Context) = sp(c).getString("token", "") ?: ""
    fun chatId(c: Context) = sp(c).getString("chat", "") ?: ""
    fun pin(c: Context) = sp(c).getString("pin", "") ?: ""
    fun trusted(c: Context) = sp(c).getString("trusted", "") ?: ""
    fun armed(c: Context) = sp(c).getBoolean("armed", false)
    fun offset(c: Context) = sp(c).getLong("offset", 0L)
    fun fails(c: Context) = sp(c).getInt("fails", 0)
    fun lastEvidence(c: Context) = sp(c).getLong("lastEv", 0L)
    fun wipePending(c: Context) = sp(c).getLong("wipeAt", 0L)

    fun set(c: Context, token: String? = null, chat: String? = null, pin: String? = null,
            trusted: String? = null, armed: Boolean? = null) {
        val e = sp(c).edit()
        token?.let { e.putString("token", it) }
        chat?.let { e.putString("chat", it) }
        pin?.let { e.putString("pin", it) }
        trusted?.let { e.putString("trusted", it) }
        armed?.let { e.putBoolean("armed", it) }
        e.apply()
    }

    fun setOffset(c: Context, v: Long) = sp(c).edit().putLong("offset", v).apply()
    fun addFail(c: Context) = sp(c).edit().putInt("fails", fails(c) + 1).apply()
    fun resetFails(c: Context) = sp(c).edit().putInt("fails", 0).apply()
    fun markEvidence(c: Context) = sp(c).edit().putLong("lastEv", System.currentTimeMillis()).apply()
    fun setWipePending(c: Context, at: Long) = sp(c).edit().putLong("wipeAt", at).apply()

    fun ready(c: Context) = token(c).isNotBlank() && chatId(c).isNotBlank() && pin(c).length >= 4
}
