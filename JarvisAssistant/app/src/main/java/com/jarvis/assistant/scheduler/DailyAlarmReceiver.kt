package com.jarvis.assistant.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jarvis.assistant.data.SettingsRepository

class DailyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<JarvisWorker>().build())

        // Android exact alarms are one-shot, so re-arm tomorrow's alarm right away.
        val settings = SettingsRepository(context)
        AlarmScheduler.schedule(context, settings.getScheduleHour(), settings.getScheduleMinute())
    }
}
