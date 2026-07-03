package com.dragonic.system.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Auto-start guard on boot if was previously enabled
                // Check shared prefs synchronously here
                val prefs = context.getSharedPreferences("dragonic_guard", Context.MODE_PRIVATE)
                if (prefs.getBoolean("guard_was_active", false)) {
                    DragonicGuardService.start(context)
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                DragonicGuardService.triggerCapture(context)
            }
        }
    }
}
