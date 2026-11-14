package com.abloy.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val acao = intent.action ?: return
        // pre-desbloqueio: nao sobe servico (sem permissao de camera/local ainda) — so o BOOT_COMPLETED (pos-desbloqueio) sobe
        if (acao == "android.intent.action.LOCKED_BOOT_COMPLETED") return
        if (acao != Intent.ACTION_BOOT_COMPLETED && acao != "android.intent.action.QUICKBOOT_POWERON") return
        if (!Prefs.armed(context) || !Prefs.ready(context)) return
        try {
            GuardService.start(context)
            Thread {
                TgBridge.sendMessage(
                    Prefs.token(context), Prefs.chatId(context),
                    "ABLOY: dispositivo reiniciado — protecao ativa (build " + Prefs.BUILD + ")"
                )
            }.start()
        } catch (_: Exception) { }
    }
}
