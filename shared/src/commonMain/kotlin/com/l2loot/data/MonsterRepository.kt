package com.l2loot.data

import com.l2loot.db.L2LootDatabase
import com.l2loot.db.Monsters

interface MonsterRepository {
    fun getAllMonsters(): List<Monsters>
}

class MonsterRepositoryImpl(
    private val database: L2LootDatabase
) : MonsterRepository {
    override fun getAllMonsters(): List<Monsters> {
        return database.monstersQueries.selectAll().executeAsList()
    }
}
