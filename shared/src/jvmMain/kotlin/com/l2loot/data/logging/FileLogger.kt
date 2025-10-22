package com.l2loot.data.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.l2loot.Config
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A file logger that writes logs to a file in the app's data directory.
 * Only enabled for debug builds.
 */
class FileLogger : LogWriter() {
    private val logFile: File? by lazy {
        if (Config.IS_DEBUG) {
            try {
                val appDataDir = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), Config.DB_DIR_NAME)
                if (!appDataDir.exists()) {
                    appDataDir.mkdirs()
                }
                val file = File(appDataDir, "l2loot-debug.log")
                
                // Clear old log on startup
                if (file.exists() && file.length() > 1_000_000) { // Clear if > 1MB
                    file.delete()
                }
                
                file
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (!Config.IS_DEBUG) return
        
        try {
            val timestamp = LocalDateTime.now().format(timeFormatter)
            val logLevel = severity.name
            val logMessage = buildString {
                append("[$timestamp] [$logLevel] [$tag] $message")
                if (throwable != null) {
                    append("\n")
                    append(throwable.stackTraceToString())
                }
                append("\n")
            }
            
            logFile?.appendText(logMessage)
        } catch (e: Exception) {
            // Ignore logging errors
        }
    }
}

