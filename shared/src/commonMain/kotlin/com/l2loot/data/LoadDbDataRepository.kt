package com.l2loot.data

import com.l2loot.L2LootDatabase
import com.l2loot.data.monsters.strategy.DropCategory
import com.l2loot.data.monsters.strategy.HPMultiplier
import com.l2loot.data.raw_data.DroplistJson
import com.l2loot.data.raw_data.MonsterJson
import com.l2loot.data.raw_data.SellableItemJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface LoadDbDataRepository {
    val progress: StateFlow<Float>
    suspend fun load()
    fun isDatabaseEmpty(): Boolean
}

class LoadDbDataRepositoryImpl(
    private val database: L2LootDatabase
) : LoadDbDataRepository {
    val _progress = MutableStateFlow(1.0f) // Start at 1.0 (complete) by default
    override val progress = _progress.asStateFlow()

    var maxCount = 0
    var currentCount = 0

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load() = withContext(Dispatchers.IO) {
        _progress.value = 0.0f // Reset to 0 when starting load
        
        val itemsText = loadResourceAsText("data/sellable_items.json")
        val monstersText = loadResourceAsText("data/monsters.json")
        val dropsText = loadResourceAsText("data/droplist.json")

        if (itemsText == null) {
            println("⚠️ sellable_items.json not found in resources")
            _progress.value = 1.0f
            return@withContext
        }

        if (monstersText == null) {
            println("⚠️ monsters.json not found in resources")
            _progress.value = 1.0f
            return@withContext
        }

        if (dropsText == null) {
            println("⚠️ droplist.json not found in resources")
            _progress.value = 1.0f
            return@withContext
        }

        val drops = json.decodeFromString<List<DroplistJson>>(dropsText)
        val monsters = json.decodeFromString<List<MonsterJson>>(monstersText)
        val items = json.decodeFromString<List<SellableItemJson>>(itemsText)

        maxCount = drops.size + monsters.size + items.size
        currentCount = 0

        val itemsJob = async { loadSellableItems(database, items) }
        val monstersJob = async { loadMonsters(database, monsters) }
        val droplistJob = async { loadDroplist(database, drops) }

        awaitAll(itemsJob, monstersJob, droplistJob)

        _progress.value = 1.0f
        println("✅ Database loaded successfully!")
    }

    private fun loadSellableItems(database: L2LootDatabase, items: List<SellableItemJson> = emptyList()) {
        println("📦 Loading sellable items...")

        database.transaction {
            items.forEach { item ->
                updateProgress()
                database.sellableItemQueries.insert(
                    item_id = item.item_id,
                    key = item.key,
                    name = item.name,
                    item_price = item.price
                )
            }
        }
    }

    private fun loadMonsters(database: L2LootDatabase, monsters: List<MonsterJson> = emptyList()) {
        println("📦 Loading monsters...")

        database.transaction {
            monsters.forEach { monster ->
                updateProgress()
                try {
                    database.monstersQueries.insert(
                        id = monster.id,
                        name = monster.name,
                        level = monster.level,
                        exp = monster.exp,
                        is_rift = monster.is_rift == 1,
                        chronicle = monster.chronicle,
                        hp_multiplier = HPMultiplier.fromValue(monster.hp_multiplier)
                    )
                } catch (e: IllegalArgumentException) {
                    println("❌ Error loading monster: id=${monster.id}, name=${monster.name}, hp_multiplier=${monster.hp_multiplier} (type: ${monster.hp_multiplier::class.simpleName})")
                    throw e
                }
            }
        }
    }

    private fun loadDroplist(database: L2LootDatabase, drops: List<DroplistJson> = emptyList()) {
        println("📦 Loading droplist...")
        var skippedCount = 0

        database.transaction {
            drops.forEach { drop ->
                updateProgress()
                val category = DropCategory.fromValue(drop.category)
                if (category != null) {
                    database.droplistQueries.insert(
                        mob_id = drop.mob_id,
                        item_id = drop.item_id,
                        min = drop.min,
                        max = drop.max,
                        chance = drop.chance,
                        category = category,
                        chronicle = drop.chronicle
                    )
                } else {
                    skippedCount++
                }
            }
        }
        
        if (skippedCount > 0) {
            println("⚠️ Skipped $skippedCount drops with unknown categories")
        }
    }

    private fun updateProgress() {
        currentCount++
        _progress.update { currentCount.toFloat() / maxCount.toFloat() }
    }
    
    override fun isDatabaseEmpty(): Boolean {
        return try {
            val monsterCount = database.monstersQueries.selectAll().executeAsList().size
            monsterCount == 0
        } catch (e: Exception) {
            true
        }
    }
    
    private fun loadResourceAsText(resourcePath: String): String? {
        return try {
            val classLoader = this::class.java.classLoader
            classLoader.getResourceAsStream(resourcePath)?.bufferedReader()?.use { it.readText() }
        } catch (e: Exception) {
            println("❌ Error loading resource $resourcePath: ${e.message}")
            null
        }
    }
}