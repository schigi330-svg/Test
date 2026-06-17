package com.jarvis.assistant.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jarvis.assistant.data.SettingsRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val settings = SettingsRepository(context)
            if (!settings.getVaultUri().isNullOrBlank() && !settings.getApiKey().isNullOrBlank()) {
                AlarmScheduler.schedule(context, settings.getScheduleHour(), settings.getScheduleMinute())
            }
        }
    }
}
