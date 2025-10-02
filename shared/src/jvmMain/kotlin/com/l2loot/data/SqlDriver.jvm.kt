package com.l2loot.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.l2loot.L2LootDatabase
import java.io.File
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbFile = File("l2loot")
        val isNewDatabase = !dbFile.exists()
        
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:l2loot", Properties())
        
        if (isNewDatabase) {
            println("🆕 Creating new database schema...")
            L2LootDatabase.Schema.create(driver)
        } else {
            println("✅ Using existing database")
        }
        
        return driver
    }
}
