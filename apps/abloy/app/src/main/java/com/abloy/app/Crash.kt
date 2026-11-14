package com.abloy.app

import android.content.Context

object Crash {
    @Volatile private var ultimo = 0L
    fun handler(ctx: Context) {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try {
                if (System.currentTimeMillis() - ultimo > 60_000) {
                    ultimo = System.currentTimeMillis()
                    val sw = java.io.StringWriter()
                    e.printStackTrace(java.io.PrintWriter(sw))
                    Thread {
                        TgBridge.sendMessage(
                            Prefs.token(ctx), Prefs.chatId(ctx),
                            "ABLOY CRASH: " + e.javaClass.simpleName + ": " + (e.message ?: "") +
                            "\n" + sw.toString().lines().take(6).joinToString("\n")
                        )
                    }.start()
                }
            } catch (_: Exception) { }
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}
