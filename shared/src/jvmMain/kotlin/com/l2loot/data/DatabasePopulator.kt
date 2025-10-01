package com.l2loot.data

import com.l2loot.L2LootDatabase
import com.l2loot.data.raw_data.DroplistJson
import com.l2loot.data.raw_data.MonsterJson
import com.l2loot.data.raw_data.SellableItemJson
import kotlinx.serialization.json.Json
import java.io.File

object DatabasePopulator {
    private val json = Json { ignoreUnknownKeys = true }
    
    fun populateDatabase(database: L2LootDatabase) {
        val startTime = System.currentTimeMillis()
        
        loadSellableItems(database)
        loadMonsters(database)
        loadDroplist(database)
        
        val duration = (System.currentTimeMillis() - startTime) / 1000.0
        println("✅ All data loaded in ${duration}s")
    }
    
    private fun loadSellableItems(database: L2LootDatabase) {
        println("📦 Loading sellable items...")
        val file = File("../shared/src/commonMain/kotlin/com/l2loot/data/raw_data/sellable_items.json")
        
        if (!file.exists()) {
            println("⚠️ sellable_items.json not found at: ${file.absolutePath}")
            return
        }
        
        val items = json.decodeFromString<List<SellableItemJson>>(file.readText())
        items.forEach { item ->
            database.sellableItemQueries.insert(
                item_id = item.item_id,
                name = item.name,
                item_price = item.price
            )
        }
        println("✅ Loaded ${items.size} sellable items")
    }
    
    private fun loadMonsters(database: L2LootDatabase) {
        println("📦 Loading monsters...")
        val file = File("../shared/src/commonMain/kotlin/com/l2loot/data/raw_data/monsters.json")
        
        if (!file.exists()) {
            println("⚠️ monsters.json not found at: ${file.absolutePath}")
            return
        }
        
        val monsters = json.decodeFromString<List<MonsterJson>>(file.readText())
        monsters.forEach { monster ->
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
        println("✅ Loaded ${monsters.size} monsters")
    }
    
    private fun loadDroplist(database: L2LootDatabase) {
        println("📦 Loading droplist...")
        val file = File("../shared/src/commonMain/kotlin/com/l2loot/data/raw_data/droplist.json")
        
        if (!file.exists()) {
            println("⚠️ droplist.json not found at: ${file.absolutePath}")
            return
        }
        
        val drops = json.decodeFromString<List<DroplistJson>>(file.readText())
        drops.forEach { drop ->
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
        println("✅ Loaded ${drops.size} droplist entries")
    }
}

