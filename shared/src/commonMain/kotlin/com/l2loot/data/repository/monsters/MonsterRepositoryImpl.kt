package com.l2loot.data.repository.monsters

import com.l2loot.L2LootDatabase
import com.l2loot.domain.model.MonsterQueryParams
import com.l2loot.domain.model.MonsterResult
import com.l2loot.domain.repository.MonsterRepository
import kotlinx.coroutines.flow.Flow

class MonsterRepositoryImpl(
    private val database: L2LootDatabase
) : MonsterRepository {

    private val strategySelector = MonsterQueryStrategySelector(database.monstersQueries)

    override fun getMonsters(params: MonsterQueryParams): Flow<Result<List<MonsterResult>>> {
        val strategy = strategySelector.selectStrategy(params)
        return strategy.execute(params)
    }
}