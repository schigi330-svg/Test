package com.jarvis.assistant.scheduler

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jarvis.assistant.JarvisApp
import com.jarvis.assistant.data.ClaudeApiClient
import com.jarvis.assistant.data.SettingsRepository
import com.jarvis.assistant.data.VaultManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * One full Jarvis cycle: read the vault, ask Claude to analyze it per the
 * instructions in CLAUDE.md, then write the result back into
 * Outputs/ and Memory/Learnings.md, and notify the user.
 */
class JarvisWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = SettingsRepository(applicationContext)
        val vault = VaultManager(applicationContext)
        val apiKey = settings.getApiKey()

        if (apiKey.isNullOrBlank() || settings.getVaultUri().isNullOrBlank()) {
            return Result.failure()
        }

        return withContext(Dispatchers.IO) {
            try {
                vault.ensureFolderStructure()
                val root = vault.getVaultRoot()
                val systemPrompt = root?.findFile("CLAUDE.md")?.let { vault.readText(it) }
                    ?: "You are a helpful personal-knowledge assistant."
                val notesText = vault.readAllNotesText()

                val userMessage = "Here is the current vault content:\n$notesText\n\n" +
                    "Follow your instructions: update Memory/Learnings.md and write today's report."

                ClaudeApiClient(apiKey).sendMessage(systemPrompt, userMessage).fold(
                    onSuccess = { responseText ->
                        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        vault.appendOrCreate("Outputs", "report-$dateStr.md", responseText)
                        vault.appendOrCreate("Memory", "Learnings.md", "## $dateStr\n$responseText")
                        settings.setLastRunResult(responseText)
                        notify("Jarvis hat ein neues Update", responseText.take(100))
                        Result.success()
                    },
                    onFailure = {
                        notify("Jarvis-Lauf fehlgeschlagen", it.message ?: "Unbekannter Fehler")
                        Result.retry()
                    }
                )
            } catch (e: Exception) {
                notify("Jarvis-Lauf fehlgeschlagen", e.message ?: "Unbekannter Fehler")
                Result.retry()
            }
        }
    }

    private fun notify(title: String, text: String) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(applicationContext, JarvisApp.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()
        manager.notify(1, notification)
    }
}
