package com.jarvis.assistant.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.data.VaultManager

@Composable
fun NoteScreen(vault: VaultManager, folder: String, fileName: String, onBack: () -> Unit) {
    val content = remember(folder, fileName) {
        vault.listFiles(folder).find { it.name == fileName }
            ?.let { vault.readText(it) }
            ?: "Datei nicht gefunden."
    }

    Scaffold(topBar = { TopAppBar(title = { Text(fileName) }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(content)
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onBack) { Text("Zurück") }
        }
    }
}
