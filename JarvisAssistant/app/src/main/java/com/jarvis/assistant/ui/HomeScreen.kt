package com.jarvis.assistant.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jarvis.assistant.data.SettingsRepository
import com.jarvis.assistant.data.VaultManager
import com.jarvis.assistant.scheduler.JarvisWorker

@Composable
fun HomeScreen(
    settings: SettingsRepository,
    vault: VaultManager,
    onPickFolder: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNote: (String, String) -> Unit
) {
    val context = LocalContext.current
    val hasVault = settings.getVaultUri() != null
    var refreshKey by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jarvis") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Einstellungen")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (!hasVault) {
                Text("Noch kein Vault verbunden.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onPickFolder) { Text("Obsidian-Vault-Ordner auswählen") }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = {
                        WorkManager.getInstance(context)
                            .enqueue(OneTimeWorkRequestBuilder<JarvisWorker>().build())
                        refreshKey++
                    }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Jetzt ausführen")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onPickFolder) { Text("Vault ändern") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                key(refreshKey) {
                    VaultManager.FOLDERS.forEach { folder ->
                        FolderSection(vault = vault, folder = folder, onOpenNote = onOpenNote)
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderSection(vault: VaultManager, folder: String, onOpenNote: (String, String) -> Unit) {
    val files = remember(folder) { vault.listFiles(folder) }
    Text(folder, style = MaterialTheme.typography.titleMedium)
    if (files.isEmpty()) {
        Text("  (leer)", style = MaterialTheme.typography.bodySmall)
    } else {
        files.forEach { file ->
            TextButton(onClick = { onOpenNote(folder, file.name.orEmpty()) }) {
                Text(file.name.orEmpty())
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
