package com.abloy.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HealReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            GuardService.stop(context)
            Thread.sleep(800)
            GuardService.start(context)
            Thread {
                Thread.sleep(1500)
                TgBridge.sendMessage(
                    Prefs.token(context), Prefs.chatId(context),
                    "ABLOY: rearme completo (notificacao, build " + Prefs.BUILD + ") — camera e GPS restituidos"
                )
            }.start()
        } catch (_: Exception) { }
    }
}
