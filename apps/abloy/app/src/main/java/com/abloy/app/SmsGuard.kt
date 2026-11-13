package com.abloy.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import java.util.concurrent.Executors

class SmsGuard : BroadcastReceiver() {
    private val exec = Executors.newSingleThreadExecutor()

    override fun onReceive(context: Context, intent: Intent) {
        if (!Prefs.armed(context)) return
        val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val from = msgs[0]?.originatingAddress ?: return
        val body = msgs.joinToString("") { it?.messageBody ?: "" }.lowercase()

        val trusted = Prefs.trusted(context)
        val pin = Prefs.pin(context).lowercase()
        if (trusted.isBlank() || pin.isBlank()) return
        if (from != trusted) return
        if (!body.contains("localizar") || !body.contains(pin)) return

        val pending = goAsync()
        exec.execute {
            try {
                val loc = GpsEngine.fix(context)
                SmsManager.getDefault().sendTextMessage(
                    from, null, "ABLOY: " + GpsEngine.mapsLink(loc), null, null)
            } finally { pending.finish() }
        }
    }
}
