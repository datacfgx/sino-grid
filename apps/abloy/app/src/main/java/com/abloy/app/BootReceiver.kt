package com.abloy.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Prefs.armed(context) && Prefs.ready(context)) GuardService.start(context)
    }
}
