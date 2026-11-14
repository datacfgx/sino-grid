package com.abloy.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val acao = intent.action ?: return
        if (acao == "android.intent.action.LOCKED_BOOT_COMPLETED") return
        if (acao != Intent.ACTION_BOOT_COMPLETED &&
            acao != "android.intent.action.QUICKBOOT_POWERON" &&
            acao != Intent.ACTION_USER_UNLOCKED) return
        if (!Prefs.armed(context) || !Prefs.ready(context)) return
        try {
            if (acao == Intent.ACTION_USER_UNLOCKED) {
                // primeiro desbloqueio pos-boot: recompoe o servico COM os tipos camera|location (exemption do A14)
                GuardService.stop(context)
                Thread.sleep(800)
            }
            GuardService.start(context)
            Thread {
                TgBridge.sendMessage(
                    Prefs.token(context), Prefs.chatId(context),
                    "ABLOY: dispositivo reiniciado — protecao ativa (" +
                    (if (acao == Intent.ACTION_USER_UNLOCKED) "pos-desbloqueio, tipos validos" else acao.substringAfterLast(".")) +
                    ", build " + Prefs.BUILD + ")"
                )
            }.start()
        } catch (_: Exception) { }
    }
}
