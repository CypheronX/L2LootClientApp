package com.l2loot.data.monsters.strategy

import kotlinx.coroutines.flow.Flow

/**
 * HP Multiplier tiers for monsters
 */
enum class HPMultiplier(val value: Double) {
    X1(1.0),
    X025(0.25),
    X05(0.5),
    X2(2.0),
    X3(3.0),
    X4(4.0),
    X5(5.0),
    X6(6.0),
    X10(10.0);

    fun getHPMultiplierLabel(): String {
        return when(this) {
            X025 -> "x1/4"
            X05 -> "x1/2"
            else -> "x${ this.value.toInt() }"
        }
    }

    companion object {
        private const val EPSILON = 0.0001  // Tolerance for comparison

        fun fromValue(value: Double): HPMultiplier {
            val result = entries.find {
                kotlin.math.abs(it.value - value) < EPSILON
            }
            if (result == null) {
                val diffs = entries.map { 
                    "${it.name}(${it.value}): diff=${kotlin.math.abs(it.value - value)}" 
                }
                throw IllegalArgumentException(
                    "No HPMultiplier for value: $value (raw bits: ${value.toBits()})\n" +
                    "Available values with differences:\n${diffs.joinToString("\n")}"
                )
            }
            return result
        }
    }
}

/**
 * Drop category classification
 * Determines the type/priority of the drop
 */
enum class DropCategory(val value: Long) {
    SPOIL(-1),
    ADENA(0),
    EQUIPMENT(1),
    MATERIALS(2);

    companion object {
        fun fromValue(value: Long): DropCategory? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Query parameters
 */
data class MonsterQueryParams(
    val minLevel: Int,
    val maxLevel: Int,
    val chronicle: String,
    val limit: Int,
    val hpMultipliers: List<HPMultiplier>? = null,
    val includeRift: Boolean = false,
    val useAynixPrices: Boolean = false
)

/**
 * Strategy interface
 */
interface MonsterQueryStrategy {
    fun execute(params: MonsterQueryParams): Flow<Result<List<MonsterResult>>>
}

/**
 * Combined drop and sellable item information
 */
data class DropItemInfo(
    val min: Int,
    val max: Int,
    val chance: Int,
    val category: DropCategory,
    val itemKey: String,
    val itemName: String,
    val itemPrice: Int
) {
    val averageQuantity: Double
        get() = (min + max) / 2.0

    val probabilityPercent: Double
        get() {
            return (chance.toDouble() / 1_000_000) * 100
        }
}

/**
 * Complete Monster query result
 */
data class MonsterResult(
    val id: Int,
    val name: String,
    val level: Int,
    val exp: Int,
    val isRift: Boolean,
    val chronicle: String,
    val hpMultiplier: HPMultiplier,
    val drops: List<DropItemInfo>
) {
    val spoils: List<DropItemInfo>
        get() {
            return drops.filter { it.category == DropCategory.SPOIL }
        }

    val adena: DropItemInfo?
        get() {
            return drops.firstOrNull { it.category == DropCategory.ADENA }
        }

    val droppedSellable: List<DropItemInfo>
        get() {
            return drops.filter { it.category == DropCategory.MATERIALS }
        }

    fun getAverageIncome(): Int {
        return spoilAverageIncome() + adenaAverageIncome() + droppedSellableAverageIncome()
    }

    fun spoilAverageIncome(): Int {
        var totalIncome = 0.0

        for (spoil in spoils) {
            if (spoil.itemPrice > 0) {
                totalIncome += (spoil.itemPrice * spoil.probabilityPercent / 100.0) * spoil.averageQuantity
            }
        }

        return totalIncome.toInt()
    }

    fun adenaAverageIncome(): Int {
        val adenaItem = adena ?: return 0
        return ((adenaItem.itemPrice * adenaItem.probabilityPercent / 100.0) * adenaItem.averageQuantity).toInt()
    }

    fun droppedSellableAverageIncome(): Int {
        var totalIncome = 0.0
        val allItems = mutableListOf<Pair<Double, Double>>()

        for (item in droppedSellable) {
            val balancedChance = minOf(item.probabilityPercent, 100.0)

            val itemValue = if (item.itemPrice > 0) {
                item.itemPrice * item.averageQuantity
            } else {
                0.0
            }

            allItems.add(Pair(balancedChance, itemValue))
        }

        if (allItems.isNotEmpty()) {
            val totalBalancedChance = maxOf(allItems.sumOf { it.first }, 100.0)

            for((balancedChance, itemValue) in allItems) {
                val realChance = balancedChance / totalBalancedChance
                totalIncome += realChance * itemValue
            }
        }

        return totalIncome.toInt()
    }
}