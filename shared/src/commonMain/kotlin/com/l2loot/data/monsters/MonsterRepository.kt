package com.l2loot.data.monsters

import com.l2loot.L2LootDatabase
import com.l2loot.Monsters

interface MonsterRepository {
    fun getAllMonsters(): List<Monsters>
    fun getMonstersInLevelRange(minLevel: Int, maxLevel: Int, chronicle: String, limit: Int): List<Monsters>
}

class MonsterRepositoryImpl(
    private val database: L2LootDatabase
) : MonsterRepository {

    override fun getAllMonsters(): List<Monsters> {
        return database.monstersQueries.selectAll().executeAsList()
    }

    override fun getMonstersInLevelRange(minLevel: Int, maxLevel: Int, chronicle: String, limit: Int): List<Monsters> {
        return database.monstersQueries.getMonstersByLevelRange(minLevel.toLong(), maxLevel.toLong(), chronicle, limit.toLong()).executeAsList()
    }
}
