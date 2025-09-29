package com.l2loot.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.l2loot.db.L2LootDatabase
import java.util.Properties

class JvmDriverFactory : DriverFactory {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver("jdbc:sqlite:l2loot.db", Properties(), L2LootDatabase.Schema)
    }
}
