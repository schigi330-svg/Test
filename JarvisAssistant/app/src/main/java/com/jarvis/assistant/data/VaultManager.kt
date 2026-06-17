package com.jarvis.assistant.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Wraps the user-selected Obsidian vault folder (granted via the system
 * folder picker / Storage Access Framework) and exposes simple read/write
 * operations against the standard Jarvis folder layout:
 *   Daily/, Projects/, Knowledge/, Memory/, Outputs/, plus a CLAUDE.md
 * instructions file at the vault root.
 */
class VaultManager(private val context: Context) {

    fun getVaultRoot(): DocumentFile? {
        val uriString = SettingsRepository(context).getVaultUri() ?: return null
        val uri = Uri.parse(uriString)
        return DocumentFile.fromTreeUri(context, uri)
    }

    fun ensureFolderStructure() {
        val root = getVaultRoot() ?: return
        FOLDERS.forEach { name ->
            if (root.findFile(name) == null) {
                root.createDirectory(name)
            }
        }
        if (root.findFile("CLAUDE.md") == null) {
            root.createFile("text/markdown", "CLAUDE.md")?.let { file ->
                writeText(file, DEFAULT_CLAUDE_MD)
            }
        }
    }

    fun listFiles(folderName: String): List<DocumentFile> {
        val root = getVaultRoot() ?: return emptyList()
        val folder = root.findFile(folderName) ?: return emptyList()
        return folder.listFiles()
            .filter { it.isFile && it.name?.endsWith(".md") == true }
            .sortedByDescending { it.lastModified() }
    }

    fun readText(file: DocumentFile): String {
        val input = context.contentResolver.openInputStream(file.uri) ?: return ""
        return input.bufferedReader().use { it.readText() }
    }

    fun writeText(file: DocumentFile, content: String) {
        context.contentResolver.openOutputStream(file.uri, "wt")?.use { out ->
            out.write(content.toByteArray())
        }
    }

    /** Appends to a file inside [folderName], creating both the folder and file if missing. */
    fun appendOrCreate(folderName: String, fileName: String, content: String) {
        val root = getVaultRoot() ?: return
        val folder = root.findFile(folderName) ?: root.createDirectory(folderName) ?: return
        val file = folder.findFile(fileName) ?: folder.createFile("text/markdown", fileName)
        file?.let {
            val existing = readText(it)
            val combined = if (existing.isBlank()) content else "$existing\n\n$content"
            writeText(it, combined)
        }
    }

    /** Concatenates every note across the given folders into one block of text for the model. */
    fun readAllNotesText(folders: List<String> = FOLDERS): String {
        val builder = StringBuilder()
        folders.forEach { folder ->
            listFiles(folder).forEach { file ->
                builder.append("\n--- $folder/${file.name} ---\n")
                builder.append(readText(file))
                builder.append("\n")
            }
        }
        return builder.toString()
    }

    companion object {
        val FOLDERS = listOf("Daily", "Projects", "Knowledge", "Memory", "Outputs")
    }
}

private const val DEFAULT_CLAUDE_MD = """# CLAUDE.md – How Jarvis works

You are Jarvis, a personal intelligence system for this Obsidian vault.

## Folders
- Daily/ — one note per day, free-form journaling
- Projects/ — active projects, roadmaps, ideas
- Knowledge/ — reference material, concepts, sources
- Memory/ — people, learnings, recurring patterns you have noticed
- Outputs/ — summaries, reports, plans you generate

## Your job, every run
1. Read the most recent Daily notes and any updated Project/Knowledge notes.
2. Identify patterns, open questions, or risks worth flagging.
3. Update Memory/Learnings.md with anything new and durable.
4. Write a short report to Outputs/ summarizing what changed and what to do next.

Be concise. Prefer bullet points only when essential. Never invent facts not present in the notes.
"""
