package com.l2loot.data.monsters

import com.l2loot.L2LootDatabase
import com.l2loot.data.monsters.strategy.MonsterQueryParams
import com.l2loot.data.monsters.strategy.MonsterQueryStrategySelector
import com.l2loot.data.monsters.strategy.MonsterResult
import kotlinx.coroutines.flow.Flow

interface MonsterRepository {
    fun getMonsters(params: MonsterQueryParams): Flow<Result<List<MonsterResult>>>
}

class MonsterRepositoryImpl(
    private val database: L2LootDatabase
) : MonsterRepository {

    private val strategySelector = MonsterQueryStrategySelector(database.monstersQueries)

    override fun getMonsters(params: MonsterQueryParams): Flow<Result<List<MonsterResult>>> {
        val strategy = strategySelector.selectStrategy(params)
        return strategy.execute(params)
    }
}
