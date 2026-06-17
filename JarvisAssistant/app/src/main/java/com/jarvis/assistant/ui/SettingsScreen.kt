package com.jarvis.assistant.ui

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.data.SettingsRepository
import com.jarvis.assistant.scheduler.AlarmScheduler

@Composable
fun SettingsScreen(settings: SettingsRepository, onBack: () -> Unit) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf(settings.getApiKey().orEmpty()) }
    var hour by remember { mutableStateOf(settings.getScheduleHour().toString()) }
    var minute by remember { mutableStateOf(settings.getScheduleMinute().toString()) }
    var saved by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Einstellungen") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text("Anthropic API-Key", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; saved = false },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Tägliche Ausführung um", style = MaterialTheme.typography.labelLarge)
            Row {
                OutlinedTextField(
                    value = hour,
                    onValueChange = { hour = it; saved = false },
                    label = { Text("Stunde") },
                    modifier = Modifier.width(120.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = minute,
                    onValueChange = { minute = it; saved = false },
                    label = { Text("Minute") },
                    modifier = Modifier.width(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                settings.setApiKey(apiKey)
                val h = hour.toIntOrNull() ?: 9
                val m = minute.toIntOrNull() ?: 0
                settings.setSchedule(h, m)
                AlarmScheduler.schedule(context, h, m)
                saved = true
            }) { Text("Speichern") }

            if (saved) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Gespeichert. Nächster Lauf um $hour:$minute Uhr.")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !AlarmScheduler.canScheduleExactAlarms(context)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Android braucht eine zusätzliche Freigabe, damit der tägliche Lauf " +
                        "pünktlich startet (\"Alarme & Erinnerungen\").",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    )
                }) { Text("Berechtigung erteilen") }
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onBack) { Text("Zurück") }
        }
    }
}
