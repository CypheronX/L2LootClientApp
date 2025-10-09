package com.l2loot.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.l2loot.L2LootDatabase
import java.io.File
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val appDataDir = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "L2Loot")
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
            println("📁 Created app data directory: ${appDataDir.absolutePath}")
        }
        
        val dbFile = File(appDataDir, "l2loot.db")
        val isNewDatabase = !dbFile.exists()
        
        val properties = Properties().apply {
            setProperty("journal_mode", "WAL")
            setProperty("busy_timeout", "5000")
        }
        
        val dbPath = dbFile.absolutePath
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$dbPath", properties)
        
        driver.execute(null, "PRAGMA journal_mode=WAL", 0)
        driver.execute(null, "PRAGMA busy_timeout=5000", 0)
        driver.execute(null, "PRAGMA synchronous=NORMAL", 0)
        
        if (isNewDatabase) {
            println("🆕 Creating new database at: $dbPath")
            L2LootDatabase.Schema.create(driver)
        } else {
            println("✅ Using existing database at: $dbPath")
        }
        
        return driver
    }
}
