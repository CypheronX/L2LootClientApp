package com.l2loot.data.repository

import com.l2loot.L2LootDatabase
import com.l2loot.domain.model.DropCategory
import com.l2loot.domain.model.HPMultiplier
import com.l2loot.domain.model.DroplistJson
import com.l2loot.domain.model.MonsterJson
import com.l2loot.domain.model.SellableItemJson
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.repository.LoadDbDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class LoadDbDataRepositoryImpl(
    private val database: L2LootDatabase,
    private val logger: LootLogger
) : LoadDbDataRepository {
    val _progress = MutableStateFlow(1.0f) // Start at 1.0 (complete) by default
    override val progress = _progress.asStateFlow()

    private var maxCount = 0
    private var currentCount = 0
    private val progressMutex = Mutex()

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load() = withContext(Dispatchers.IO) {
        _progress.value = 0.0f // Reset to 0 when starting load
        
        val itemsText = loadResourceAsText("data/sellable_items.json")
        val monstersText = loadResourceAsText("data/monsters.json")
        val dropsText = loadResourceAsText("data/droplist.json")

        if (itemsText == null) {
            logger.warn("⚠️ sellable_items.json not found in resources")
            _progress.value = 1.0f
            return@withContext
        }

        if (monstersText == null) {
            logger.warn("⚠️ monsters.json not found in resources")
            _progress.value = 1.0f
            return@withContext
        }

        if (dropsText == null) {
            logger.warn("⚠️ droplist.json not found in resources")
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
        logger.info("✅ Database loaded successfully!")
    }

    private suspend fun loadSellableItems(database: L2LootDatabase, items: List<SellableItemJson> = emptyList()) {
        logger.info("📦 Loading sellable items...")

        val batchSize = 100
        items.chunked(batchSize).forEach { batch ->
            database.transaction {
                batch.forEach { item ->
                    database.sellableItemQueries.insert(
                        item_id = item.item_id,
                        key = item.key,
                        name = item.name,
                        item_price = item.price
                    )
                }
            }
            updateProgressBulk(batch.size)
        }
    }

    private suspend fun loadMonsters(database: L2LootDatabase, monsters: List<MonsterJson> = emptyList()) {
        logger.info("📦 Loading monsters...")

        val batchSize = 100
        monsters.chunked(batchSize).forEach { batch ->
            database.transaction {
                batch.forEach { monster ->
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
                        logger.error("❌ Error loading monster: id=${monster.id}, name=${monster.name}, hp_multiplier=${monster.hp_multiplier} (type: ${monster.hp_multiplier::class.simpleName})")
                        throw e
                    }
                }
            }
            updateProgressBulk(batch.size)
        }
    }

    private suspend fun loadDroplist(database: L2LootDatabase, drops: List<DroplistJson> = emptyList()) {
        logger.info("📦 Loading droplist...")
        var skippedCount = 0

        val batchSize = 100
        drops.chunked(batchSize).forEach { batch ->
            database.transaction {
                batch.forEach { drop ->
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
            updateProgressBulk(batch.size)
        }
        
        if (skippedCount > 0) {
            logger.info("⚠️ Skipped $skippedCount drops with unknown categories")
        }
    }

    private suspend fun updateProgressBulk(count: Int) {
        progressMutex.withLock {
            currentCount += count
            val newProgress = currentCount.toFloat() / maxCount.toFloat()
            _progress.value = newProgress
        }
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
            logger.error("❌ Error loading resource $resourcePath: ${e.message}")
            null
        }
    }
}
