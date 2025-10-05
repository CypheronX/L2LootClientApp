package com.l2loot.data.monsters.strategy

/**
 * HP Multiplier tiers for monsters
 */
enum class HPMultiplier(val value: Double) {
    X05(0.5),
    X1(1.0),
    X2(2.0),
    X3(3.0),
    X4(4.0),
    X5(5.0),
    X6(6.0);

    companion object {
        fun fromValue(value: Double): HPMultiplier? =
            values().find { it.value == value }
    }
}

/**
 * Drop category classification
 * Determines the type/priority of the drop
 */
enum class DropCategory(val value: Int) {
    SPOIL(-1),
    ADENA(0),
    EQUIPMENT(1),
    MATERIALS(2);

    companion object {
        fun fromValue(value: Int): DropCategory? =
            values().find { it.value == value }
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
    val includeRift: Boolean = false
)

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
)

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
    val dropItem: DropItemInfo
)