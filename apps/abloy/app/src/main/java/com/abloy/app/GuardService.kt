package com.abloy.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.IBinder
import org.json.JSONObject
import java.util.concurrent.Executors

class GuardService : Service() {
    private val exec = Executors.newSingleThreadExecutor()
    private var running = false
    private var alarm: android.media.Ringtone? = null

    companion object {
        const val CH = "protecao"
        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, GuardService::class.java))
        }
        fun stop(ctx: Context) { ctx.stopService(Intent(ctx, GuardService::class.java)) }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Evidence.aoFalhar = { postarCura() }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH, getString(R.string.canal_protecao), NotificationManager.IMPORTANCE_LOW).apply {
            description = getString(R.string.canal_desc)
        })
        val pi = PendingIntent.getActivity(this, 0, Intent(this, CalcActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE)
        val n: Notification = Notification.Builder(this, CH)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle(getString(R.string.canal_protecao))
            .setContentText(getString(R.string.canal_desc))
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
        val tipo = if (android.os.Build.VERSION.SDK_INT >= 29)
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        androidx.core.app.ServiceCompat.startForeground(this, 1, n, tipo)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!running) {
            running = true
            exec.execute { pollLoop() }
        }
        return START_STICKY
    }

    private fun pollLoop() {
        while (Prefs.armed(this) && Prefs.ready(this)) {
            val resp = TgBridge.getUpdates(Prefs.token(this), Prefs.offset(this)) ?: continue
            try {
                val arr = JSONObject(resp).getJSONArray("result")
                for (i in 0 until arr.length()) {
                    val upd = arr.getJSONObject(i)
                    Prefs.setOffset(this, upd.getLong("update_id") + 1)
                    val msg = upd.optJSONObject("message") ?: continue
                    val chat = msg.optJSONObject("chat")?.optString("id") ?: continue
                    if (chat != Prefs.chatId(this)) continue
                    val text = msg.optString("text", "").trim()
                    handle(text.lowercase())
                }
            } catch (_: Exception) { }
        }
        running = false
        stopSelf()
    }

    private fun handle(cmd: String) {
        val t = Prefs.token(this); val c = Prefs.chatId(this)
        when {
            cmd == "/localizar" -> {
                val loc = GpsEngine.fix(this)
                TgBridge.sendMessage(t, c, "ABLOY localizacao\n" + GpsEngine.mapsLink(loc))
            }
            cmd == "/foto" -> Evidence.capture(this) { ok ->
                if (!ok) {
                    Thread { TgBridge.sendMessage(t, c, "ABLOY: falha ao capturar foto — toque na notificacao para rearmar") }.start()
                    postarCura()
                }
            }
            cmd == "/status" -> {
                val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val admin = ComponentName(this, GuardReceiver::class.java)
                TgBridge.sendMessage(t, c, "ABLOY status build=" + Prefs.BUILD + "\nprotecao: ${if (Prefs.armed(this)) "armada" else "desarmada"}\ndevice admin: ${dpm.isAdminActive(admin)}\noverlay: " + android.provider.Settings.canDrawOverlays(this) + "\nlocal: " + GpsEngine.mapsLink(GpsEngine.fix(this, 5000)))
            }
            cmd == "/alarme" -> {
                stopAlarm()
                alarm = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                alarm?.play()
                TgBridge.sendMessage(t, c, "ABLOY: alarme tocando. /parar para silenciar.")
            }
            cmd == "/parar" -> { stopAlarm(); TgBridge.sendMessage(t, c, "ABLOY: alarme silenciado.") }
            cmd == "/reiniciar" -> {
                TgBridge.sendMessage(t, c, "ABLOY: reiniciando servico (build " + Prefs.BUILD + ")")
                GuardService.stop(this); Thread.sleep(800); GuardService.start(this)
            }
            cmd == "/trancar" -> {
                val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                dpm.lockNow()
                TgBridge.sendMessage(t, c, "ABLOY: tela travada.")
            }
            cmd == "/wipe" -> {
                Prefs.setWipePending(this, System.currentTimeMillis() + 60_000)
                TgBridge.sendMessage(t, c, "ABLOY: responda SIM em 60s para confirmar o wipe.")
            }
            cmd == "sim" && System.currentTimeMillis() < Prefs.wipePending(this) -> {
                Prefs.setWipePending(this, 0)
                val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                TgBridge.sendMessage(t, c, "ABLOY: wipe iniciado. O aparelho sera apagado. FRP protege contra reuso.")
                dpm.wipeData(0)
            }
            cmd.startsWith("/destravar") -> {
                val parts = cmd.split("\\s+".toRegex())
                if (parts.size == 2 && parts[1].length >= 4) {
                    Prefs.set(this, pin = parts[1])
                    TgBridge.sendMessage(t, c, "ABLOY: PIN atualizado.")
                } else {
                    TgBridge.sendMessage(t, c, "ABLOY: use /destravar <novoPIN> (min 4 digitos)")
                }
            }
        }
    }

    private fun stopAlarm() { try { alarm?.stop() } catch (_: Exception) { }; alarm = null }

    fun postarCura() {
        try {
            val pi = android.app.PendingIntent.getBroadcast(
                this, 7, Intent(this, HealReceiver::class.java),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
            val n = Notification.Builder(this, CH)
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentTitle("ABLOY: protecao parcial apos reinicio")
                .setContentText("Toque aqui para restituir camera e GPS (2 segundos)")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
            val nm = getSystemService(android.app.NotificationManager::class.java)
            nm.notify(7, n)
        } catch (_: Exception) { }
    }

    override fun onDestroy() { stopAlarm(); running = false; super.onDestroy() }
}
