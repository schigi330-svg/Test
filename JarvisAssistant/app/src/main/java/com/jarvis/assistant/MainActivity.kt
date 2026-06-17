package com.jarvis.assistant

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jarvis.assistant.data.SettingsRepository
import com.jarvis.assistant.data.VaultManager
import com.jarvis.assistant.ui.HomeScreen
import com.jarvis.assistant.ui.NoteScreen
import com.jarvis.assistant.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    private lateinit var settings: SettingsRepository
    private lateinit var vault: VaultManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SettingsRepository(this)
        vault = VaultManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            uri?.let {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                settings.setVaultUri(it.toString())
                vault.ensureFolderStructure()
            }
        }

        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                settings = settings,
                                vault = vault,
                                onPickFolder = { pickFolder.launch(null) },
                                onOpenSettings = { navController.navigate("settings") },
                                onOpenNote = { folder, name -> navController.navigate("note/$folder/$name") }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(settings = settings, onBack = { navController.popBackStack() })
                        }
                        composable("note/{folder}/{name}") { backStackEntry ->
                            val folder = backStackEntry.arguments?.getString("folder").orEmpty()
                            val name = backStackEntry.arguments?.getString("name").orEmpty()
                            NoteScreen(
                                vault = vault,
                                folder = folder,
                                fileName = name,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
