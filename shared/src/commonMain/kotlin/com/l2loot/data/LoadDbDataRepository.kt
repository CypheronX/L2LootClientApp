package com.l2loot.data

import com.l2loot.L2LootDatabase
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
import java.io.File

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
        val itemsFile = File("../shared/src/commonMain/kotlin/com/l2loot/data/raw_data/sellable_items.json")
        val monstersFile = File("../shared/src/commonMain/kotlin/com/l2loot/data/raw_data/monsters.json")
        val dropsFile = File("../shared/src/commonMain/kotlin/com/l2loot/data/raw_data/droplist.json")

        if (!itemsFile.exists()) {
            println("⚠️ sellable_items.json not found at: ${itemsFile.absolutePath}")
            _progress.value = 1.0f
            return@withContext
        }

        if (!monstersFile.exists()) {
            println("⚠️ monsters.json not found at: ${monstersFile.absolutePath}")
            _progress.value = 1.0f
            return@withContext
        }

        if (!dropsFile.exists()) {
            println("⚠️ droplist.json not found at: ${dropsFile.absolutePath}")
            _progress.value = 1.0f
            return@withContext
        }

        val drops = json.decodeFromString<List<DroplistJson>>(dropsFile.readText())
        val monsters = json.decodeFromString<List<MonsterJson>>(monstersFile.readText())
        val items = json.decodeFromString<List<SellableItemJson>>(itemsFile.readText())

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

    private fun loadMonsters(database: L2LootDatabase, monsters: List<MonsterJson> = emptyList()) {
        println("📦 Loading monsters...")

        monsters.forEach { monster ->
            updateProgress()
            database.monstersQueries.insert(
                id = monster.id,
                name = monster.name,
                level = monster.level,
                exp = monster.exp,
                is_rift = monster.is_rift == 1,
                chronicle = monster.chronicle,
                hp_multiplier = monster.hp_multiplier
            )
        }
    }

    private fun loadDroplist(database: L2LootDatabase, drops: List<DroplistJson> = emptyList()) {
        println("📦 Loading droplist...")

        drops.forEach { drop ->
            updateProgress()
            database.droplistQueries.insert(
                mob_id = drop.mob_id,
                item_id = drop.item_id,
                min = drop.min,
                max = drop.max,
                chance = drop.chance,
                category = drop.category,
                chronicle = drop.chronicle
            )
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
}