package com.abloy.app

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class GuardReceiver : DeviceAdminReceiver() {
    override fun onPasswordFailed(context: Context, intent: Intent) {
        if (Prefs.armed(context)) Evidence.onFailedUnlock(context)
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        Prefs.resetFails(context)
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        "Desativar a protecao ABLOY? Isso exige o painel do proprietario (PIN)."

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            if (Prefs.armed(context) && Prefs.ready(context)) GuardService.start(context)
        }
        super.onReceive(context, intent)
    }
}
