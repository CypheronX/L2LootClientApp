package com.l2loot.updater

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.WindowPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Main entry point for the L2Loot Updater
 * 
 * Arguments:
 * --download-url <url>      URL to download the update ZIP
 * --install-path <path>     Path to app installation directory
 * --app-exe <path>          Path to app executable
 * --current-version <ver>   Current version
 * --new-version <ver>       New version (optional if only checking)
 * --check-update-url <url>  URL to check for updates
 * --github-token <token>    GitHub token for authentication (optional)
 */
fun main(args: Array<String>) {
    val arguments = parseArguments(args)
    
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "L2Loot Updater",
            undecorated = true,
            transparent = true,
            state = WindowState(
                width = 500.dp,
                height = 300.dp,
                position = WindowPosition(Alignment.Center)
            ),
            resizable = false,
            alwaysOnTop = true
        ) {
            UpdaterWindow(
                arguments = arguments,
                onComplete = { success, scope ->
                    if (success) {
                        // Launch the updated app
                        scope.launch {
                            launchApp(arguments.appExePath)
                            exitApplication()
                        }
                    } else {
                        exitApplication()
                    }
                }
            )
        }
    }
}

/**
 * Parse command line arguments
 */
fun parseArguments(args: Array<String>): UpdaterArguments {
    val argsMap = mutableMapOf<String, String>()
    
    var i = 0
    while (i < args.size) {
        if (args[i].startsWith("--") && i + 1 < args.size) {
            argsMap[args[i]] = args[i + 1]
            i += 2
        } else {
            i++
        }
    }
    
    return UpdaterArguments(
        downloadUrl = argsMap["--download-url"] ?: "",
        installPath = argsMap["--install-path"] ?: error("Missing --install-path argument"),
        appExePath = argsMap["--app-exe"] ?: error("Missing --app-exe argument"),
        currentVersion = argsMap["--current-version"] ?: "Unknown",
        newVersion = argsMap["--new-version"] ?: "Unknown",
        checkUpdateUrl = argsMap["--check-update-url"] ?: error("Missing --check-update-url argument"),
        githubToken = argsMap["--github-token"]
    )
}

/**
 * Launch the updated app and exit updater
 */
suspend fun launchApp(appExePath: String) = withContext(Dispatchers.IO) {
    try {
        // Small delay to ensure updater window closes
        delay(500)
        
        val appExe = File(appExePath)
        if (appExe.exists()) {
            ProcessBuilder(appExe.absolutePath, "--skip-update-check")
                .directory(appExe.parentFile)
                .start()
        }
    } catch (e: Exception) {
        println("Failed to launch app: ${e.message}")
    }
}

data class UpdaterArguments(
    val downloadUrl: String,
    val installPath: String,
    val appExePath: String,
    val currentVersion: String,
    val newVersion: String,
    val checkUpdateUrl: String,
    val githubToken: String? = null
)

