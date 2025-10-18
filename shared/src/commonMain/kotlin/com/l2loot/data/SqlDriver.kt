package com.l2loot.data

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.l2loot.Droplist
import com.l2loot.L2LootDatabase
import com.l2loot.Monsters
import com.l2loot.domain.model.DropCategory
import com.l2loot.domain.model.HPMultiplier

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): L2LootDatabase {
    val hpMultiplierAdapter = object : ColumnAdapter<HPMultiplier, Double> {
        override fun decode(databaseValue: Double): HPMultiplier {
            return HPMultiplier.fromValue(databaseValue)
        }

        override fun encode(value: HPMultiplier): Double {
            return value.value
        }
    }

    val dropCategoryAdapter = object : ColumnAdapter<DropCategory, Long> {
        override fun decode(databaseValue: Long): DropCategory {
            return DropCategory.fromValue(databaseValue)
                ?: throw IllegalArgumentException("Unknown drop category in database: $databaseValue")
        }

        override fun encode(value: DropCategory): Long {
            return value.value.toLong()
        }
    }

    val driver = driverFactory.createDriver()
    val database = L2LootDatabase(
        driver = driver,
        monstersAdapter = Monsters.Adapter(
            hp_multiplierAdapter = hpMultiplierAdapter
        ),
        droplistAdapter = Droplist.Adapter(
            categoryAdapter = dropCategoryAdapter
        )
    )

    return database
}

