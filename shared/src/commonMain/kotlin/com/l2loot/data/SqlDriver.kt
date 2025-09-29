package com.l2loot.data

import app.cash.sqldelight.db.SqlDriver
import com.l2loot.db.L2LootDatabase

interface DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): L2LootDatabase {
    val driver = driverFactory.createDriver()
    val database = L2LootDatabase(driver)

    return database
}

