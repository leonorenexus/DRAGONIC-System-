package com.dragonic.system

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DragonicApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val guardChannel = NotificationChannel(
            CHANNEL_GUARD,
            "DRAGONIC Guard",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "DRAGONIC System active protection"
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERT,
            "DRAGONIC Alert",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Intruder detected alerts"
        }

        manager.createNotificationChannels(listOf(guardChannel, alertChannel))
    }

    companion object {
        const val CHANNEL_GUARD = "dragonic_guard"
        const val CHANNEL_ALERT = "dragonic_alert"
    }
}
