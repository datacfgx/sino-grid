package com.abloy.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val acao = intent.action ?: return
        if (!acao.endsWith("BOOT_COMPLETED") && acao != "android.intent.action.QUICKBOOT_POWERON") return
        if (!Prefs.armed(context) || !Prefs.ready(context)) return
        try {
            GuardService.start(context)
            Thread {
                TgBridge.sendMessage(
                    Prefs.token(context), Prefs.chatId(context),
                    "ABLOY: dispositivo reiniciado — protecao ativa (" + acao.substringAfterLast(".") + ", build " + Prefs.BUILD + ")"
                )
            }.start()
        } catch (_: Exception) { }
    }
}
