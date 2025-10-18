package com.l2loot.data

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.l2loot.BuildConfig
import com.l2loot.L2LootDatabase
import com.l2loot.domain.logging.LootLogger
import java.io.File
import java.sql.DriverManager
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val appDataDir = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "L2Loot")
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
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
            if (BuildConfig.DEBUG) {
                println("🆕 Creating new database at: $dbPath")
            }
            L2LootDatabase.Schema.create(driver)
        } else {
            if (BuildConfig.DEBUG) {
                println("✅ Using existing database at: $dbPath")
            }

            val currentVersion = getCurrentDatabaseVersion(dbPath)
            val latestVersion = L2LootDatabase.Schema.version.toLong()
            
            if (currentVersion < latestVersion) {
                if (BuildConfig.DEBUG) {
                    println("🔄 Running migrations from version $currentVersion to $latestVersion")
                }

                try {
                    L2LootDatabase.Schema.migrate(
                        driver = driver,
                        oldVersion = currentVersion,
                        newVersion = latestVersion,
                        AfterVersion(1) { driver ->
                            if (BuildConfig.DEBUG) {
                                println("  📝 Rebuilt droplist table to support duplicate drops (double spoils)")
                            }
                        }
                        // Add more migrations here as needed:
                        // AfterVersion(2) { driver ->
                        //     if (BuildConfig.DEBUG) {
                        //         println("  📝 Description of migration 2...")
                        //     }
                        // }
                    )
                    
                    // Manually set the database version (SQLDelight doesn't always do this correctly)
                    driver.execute(null, "PRAGMA user_version = $latestVersion", 0)

                    if (BuildConfig.DEBUG) {
                        println("🎉 Migrations completed successfully!")
                    }
                    
                    // Verify version was updated
                    val newVersion = getCurrentDatabaseVersion(dbPath)
                    if (BuildConfig.DEBUG) {
                        println("✅ Database version is now: $newVersion")
                    }
                    
                    if (newVersion != latestVersion) {
                        if (BuildConfig.DEBUG) {
                            println("⚠️ Warning: Version mismatch! Expected $latestVersion but got $newVersion")
                        }
                    }
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        println("❌ Migration failed: ${e.message}")
                        e.printStackTrace()
                    }
                    throw e
                }
            } else if (currentVersion == latestVersion) {
                if (BuildConfig.DEBUG) {
                    println("✅ Database is up to date (version $currentVersion)")
                }
            } else {
                if (BuildConfig.DEBUG) {
                    println("⚠️ Database version ($currentVersion) is newer than app version ($latestVersion)")
                }
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
