package com.l2loot.data.monsters.strategy

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.l2loot.GetMonstersByLevelRange
import com.l2loot.GetMonstersByLevelRangeAndHP
import com.l2loot.GetMonstersByLevelRangeAndHPIncludeRift
import com.l2loot.GetMonstersByLevelRangeIncludeRift
import com.l2loot.MonstersQueries
import jdk.jfr.internal.OldObjectSample.emit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class BasicMonsterQueryStrategy(
    private val queries: MonstersQueries
) : MonsterQueryStrategy {

    override fun execute(params: MonsterQueryParams): Flow<Result<List<MonsterResult>>> {
        return queries.getMonstersByLevelRange(
            minLevel = params.minLevel.toLong(),
            maxLevel = params.maxLevel.toLong(),
            chronicle = params.chronicle,
            useAynixPrices = if (params.useAynixPrices) 1 else 0
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                val monsters = groupRowsByMonster(rows)
                    .sortedByDescending { it.getAverageIncome() }
                    .take(params.limit)
                Result.success(monsters)
            }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }

    private fun groupRowsByMonster(
        rows: List<GetMonstersByLevelRange>
    ): List<MonsterResult> {
        return rows
            .groupBy { it.id }
            .map { (_, monsterRows) ->
                val first = monsterRows.first()

                MonsterResult(
                    id = first.id.toInt(),
                    name = first.name,
                    level = first.level.toInt(),
                    exp = first.exp.toInt(),
                    isRift = first.is_rift == true,
                    chronicle = first.chronicle,
                    hpMultiplier = first.hp_multiplier,
                    drops = monsterRows.map { row ->
                        DropItemInfo(
                            min = row.min.toInt(),
                            max = row.max.toInt(),
                            chance = row.chance.toInt(),
                            category = row.category,
                            itemKey = row.item_key,
                            itemName = row.item_name,
                            itemPrice = row.item_price.toInt()
                        )
                    }
                )
            }
    }
}

/**
 * Strategy 2: HP filtered
 */
class HPFilteredMonsterQueryStrategy(
    private val queries: MonstersQueries
) : MonsterQueryStrategy {

    override fun execute(params: MonsterQueryParams): Flow<Result<List<MonsterResult>>> {
        requireNotNull(params.hpMultipliers) { "HP multipliers required" }
        require(params.hpMultipliers.isNotEmpty()) { "HP multipliers cannot be empty" }

        return queries.getMonstersByLevelRangeAndHP(
            minLevel = params.minLevel.toLong(),
            maxLevel = params.maxLevel.toLong(),
            hpMultipliers = params.hpMultipliers,
            chronicle = params.chronicle,
            useAynixPrices = if (params.useAynixPrices) 1 else 0
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                val monsters = groupRowsByMonster(rows)
                    .sortedByDescending { it.getAverageIncome() }
                    .take(params.limit)
                Result.success(monsters)
            }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }

    private fun groupRowsByMonster(
        rows: List<GetMonstersByLevelRangeAndHP>
    ): List<MonsterResult> {
        return rows
            .groupBy { it.id }
            .map { (_, monsterRows) ->
                val first = monsterRows.first()

                MonsterResult(
                    id = first.id.toInt(),
                    name = first.name,
                    level = first.level.toInt(),
                    exp = first.exp.toInt(),
                    isRift = first.is_rift == true,
                    chronicle = first.chronicle,
                    hpMultiplier = first.hp_multiplier,
                    drops = monsterRows.map { row ->
                        DropItemInfo(
                            min = row.min.toInt(),
                            max = row.max.toInt(),
                            chance = row.chance.toInt(),
                            category = row.category,
                            itemKey = row.item_key,
                            itemName = row.item_name,
                            itemPrice = row.item_price.toInt()
                        )
                    }
                )
            }
    }
}

/**
 * Strategy 3: Include rift
 */
class RiftIncludedMonsterQueryStrategy(
    private val queries: MonstersQueries
) : MonsterQueryStrategy {

    override fun execute(params: MonsterQueryParams): Flow<Result<List<MonsterResult>>> {
        return queries.getMonstersByLevelRangeIncludeRift(
            minLevel = params.minLevel.toLong(),
            maxLevel = params.maxLevel.toLong(),
            chronicle = params.chronicle,
            useAynixPrices = if (params.useAynixPrices) 1 else 0
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                val monsters = groupRowsByMonster(rows)
                    .sortedByDescending { it.getAverageIncome() }
                    .take(params.limit)
                Result.success(monsters)
            }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }

    private fun groupRowsByMonster(
        rows: List<GetMonstersByLevelRangeIncludeRift>
    ): List<MonsterResult> {
        return rows
            .groupBy { it.id }
            .map { (_, monsterRows) ->
                val first = monsterRows.first()

                MonsterResult(
                    id = first.id.toInt(),
                    name = first.name,
                    level = first.level.toInt(),
                    exp = first.exp.toInt(),
                    isRift = first.is_rift == true,
                    chronicle = first.chronicle,
                    hpMultiplier = first.hp_multiplier,
                    drops = monsterRows.map { row ->
                        DropItemInfo(
                            min = row.min.toInt(),
                            max = row.max.toInt(),
                            chance = row.chance.toInt(),
                            category = row.category,
                            itemKey = row.item_key,
                            itemName = row.item_name,
                            itemPrice = row.item_price.toInt()
                        )
                    }
                )
            }
    }
}

/**
 * Strategy 4: HP filtered + include rift
 */
class HPFilteredRiftIncludedMonsterQueryStrategy(
    private val queries: MonstersQueries
) : MonsterQueryStrategy {

    override fun execute(params: MonsterQueryParams): Flow<Result<List<MonsterResult>>> {
        requireNotNull(params.hpMultipliers) { "HP multipliers required" }
        require(params.hpMultipliers.isNotEmpty()) { "HP multipliers cannot be empty" }

        return queries.getMonstersByLevelRangeAndHPIncludeRift(
            minLevel = params.minLevel.toLong(),
            maxLevel = params.maxLevel.toLong(),
            hpMultipliers = params.hpMultipliers,
            chronicle = params.chronicle,
            useAynixPrices = if (params.useAynixPrices) 1 else 0
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                val monsters = groupRowsByMonster(rows)
                    .sortedByDescending { it.getAverageIncome() }
                    .take(params.limit)
                Result.success(monsters)
            }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }

    private fun groupRowsByMonster(
        rows: List<GetMonstersByLevelRangeAndHPIncludeRift>
    ): List<MonsterResult> {
        return rows
            .groupBy { it.id }
            .map { (_, monsterRows) ->
                val first = monsterRows.first()

                MonsterResult(
                    id = first.id.toInt(),
                    name = first.name,
                    level = first.level.toInt(),
                    exp = first.exp.toInt(),
                    isRift = first.is_rift == true,
                    chronicle = first.chronicle,
                    hpMultiplier = first.hp_multiplier,
                    drops = monsterRows.map { row ->
                        DropItemInfo(
                            min = row.min.toInt(),
                            max = row.max.toInt(),
                            chance = row.chance.toInt(),
                            category = row.category,
                            itemKey = row.item_key,
                            itemName = row.item_name,
                            itemPrice = row.item_price.toInt()
                        )
                    }
                )
            }
    }
}

/**
 * Strategy Selector
 */
class MonsterQueryStrategySelector(
    private val queries: MonstersQueries
) {
    private val basicStrategy = BasicMonsterQueryStrategy(queries)
    private val hpFilteredStrategy = HPFilteredMonsterQueryStrategy(queries)
    private val riftIncludedStrategy = RiftIncludedMonsterQueryStrategy(queries)
    private val hpFilteredRiftIncludedStrategy = HPFilteredRiftIncludedMonsterQueryStrategy(queries)

    fun selectStrategy(params: MonsterQueryParams): MonsterQueryStrategy {
        val hasHPFilter = params.hpMultipliers != null && params.hpMultipliers.isNotEmpty()

        return when {
            hasHPFilter && params.includeRift -> hpFilteredRiftIncludedStrategy
            hasHPFilter -> hpFilteredStrategy
            params.includeRift -> riftIncludedStrategy
            else -> basicStrategy
        }
    }
}
