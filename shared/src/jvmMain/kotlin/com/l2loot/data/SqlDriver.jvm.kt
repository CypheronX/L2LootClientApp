package com.l2loot.data

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.l2loot.L2LootDatabase
import java.io.File
import java.sql.DriverManager
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
            
            // Get current database version
            val currentVersion = getCurrentDatabaseVersion(dbPath)
            val latestVersion = L2LootDatabase.Schema.version.toLong()
            
            if (currentVersion < latestVersion) {
                println("🔄 Running migrations from version $currentVersion to $latestVersion")
                
                // Run migrations with optional callbacks for data seeding
                L2LootDatabase.Schema.migrate(
                    driver = driver,
                    oldVersion = currentVersion,
                    newVersion = latestVersion,
                    // Example: Seed data after a specific migration version
                    // AfterVersion(1) { driver ->
                    //     println("  📝 Seeding data after migration to version 1...")
                    //     driver.execute(null, "INSERT INTO sellable_item (item_id, key, name, item_price) VALUES (99999, 'new_item', 'New Item', 1000)", 0)
                    // },
                    // AfterVersion(2) { driver ->
                    //     println("  📝 Updating existing data after migration to version 2...")
                    //     driver.execute(null, "UPDATE user_settings SET new_feature_enabled = 1 WHERE id = 1", 0)
                    // }
                )
                
                println("🎉 Migrations completed successfully!")
            } else if (currentVersion == latestVersion) {
                println("✅ Database is up to date (version $currentVersion)")
            } else {
                println("⚠️ Database version ($currentVersion) is newer than app version ($latestVersion)")
            }
        }
        
        return driver
    }
    
    /**
     * Gets the current database version using direct JDBC connection
     */
    private fun getCurrentDatabaseVersion(dbPath: String): Long {
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA user_version").use { resultSet ->
                    return if (resultSet.next()) {
                        resultSet.getLong(1)
                    } else {
                        0L
                    }
                }
            }
        }
    }
}
