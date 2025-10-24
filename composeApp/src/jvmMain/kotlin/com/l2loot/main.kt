package com.l2loot

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.l2loot.di.appModule
import com.l2loot.di.initKoin
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.koin.core.context.GlobalContext
import java.io.File

fun main(args: Array<String>) {
    if (Config.IS_DEBUG) {
        writeDebugConfigToFile()
    }
    
    initKoin {
        modules(appModule)
    }
    
    val skipUpdateCheck = args.contains("--skip-update-check")
    
    if (!skipUpdateCheck && Config.BUILD_FLAVOR != "dev") {
        try {
            val logger = GlobalContext.get().get<com.l2loot.domain.logging.LootLogger>()
            val installer = UpdateInstaller(logger)
            installer.launchUpdaterAndCheckForUpdates()
            return
        } catch (e: Exception) {
            println("Failed to launch updater, continuing with main app: ${e.message}")
        }
    }
    
    application {
        var spoilLogoPainter by remember {
            mutableStateOf<Painter?>(null)
        }
        val windowRatio = rememberWindowState(size = DpSize(1280.dp, 720.dp))

        LaunchedEffect(Unit) {
            try {
                val spoilLogoBytes = Res.readBytes("files/app_icon/spoil_logo.png")

                if (spoilLogoBytes.isNotEmpty()) {
                    val imageBitmap = spoilLogoBytes.decodeToImageBitmap()
                    spoilLogoPainter = BitmapPainter(imageBitmap)
                }
            } catch (e: Exception) {
                if (Config.IS_DEBUG) {
                    println("Failed to load spoil icon: ${e.message}")
                }
            }
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "L2Loot",
            icon = spoilLogoPainter,
            state = windowRatio,
            onKeyEvent = {
                it.key == Key.Escape
            }
        ) {
            App()
        }
    }
}

private fun writeDebugConfigToFile() {
    try {
        val appDataDir = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), Config.DB_DIR_NAME)
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }
        val logFile = File(appDataDir, "l2loot-debug.log")
        
        // Clear old log if too large
        if (logFile.exists() && logFile.length() > 1_000_000) {
            logFile.delete()
        }
        
        val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        logFile.appendText("""
            [$timestamp] [INFO] [L2Loot] === App Starting ===
            [$timestamp] [INFO] [L2Loot] VERSION: ${Config.VERSION_NAME}
            [$timestamp] [INFO] [L2Loot] FLAVOR: ${Config.BUILD_FLAVOR}
            [$timestamp] [INFO] [L2Loot] APP_NAME: ${Config.APP_NAME}
            [$timestamp] [INFO] [L2Loot] ANALYTICS_URL: ${Config.ANALYTICS_URL}
            [$timestamp] [INFO] [L2Loot] SELLABLE_ITEMS_URL: ${Config.SELLABLE_ITEMS_URL}
            [$timestamp] [INFO] [L2Loot] ANONYMOUS_AUTH_URL: ${Config.ANONYMOUS_AUTH_URL}
            [$timestamp] [INFO] [L2Loot] EXTERNAL_LINKS_URL: ${Config.EXTERNAL_LINKS_URL}
            [$timestamp] [INFO] [L2Loot] GITHUB_RELEASE_REPO: ${Config.GITHUB_RELEASE_REPO}
            [$timestamp] [INFO] [L2Loot] GITHUB_TOKEN present: ${Config.GITHUB_TOKEN.isNotEmpty()}
            [$timestamp] [INFO] [L2Loot] ==================
            
        """.trimIndent())
    } catch (e: Exception) {
        // Ignore logging errors
    }
}