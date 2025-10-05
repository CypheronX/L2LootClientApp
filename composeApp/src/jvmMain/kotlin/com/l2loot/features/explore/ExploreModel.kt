package com.l2loot.features.explore

import com.l2loot.data.monsters.strategy.MonsterQueryParams
import com.l2loot.data.monsters.strategy.MonsterResult

internal data class ExploreScreenState(
    val monsters: List<MonsterResult>,
    val chronicle: String,
    val minLevel: String,
    val maxLevel: String,
    val limit: String,
    val showRiftMobs: Boolean,
    val useAynixPrices: Boolean,
    val isRefreshing: Boolean,
) {
    companion object {
        fun initial() = ExploreScreenState(
            monsters = emptyList(),
            chronicle = "c5",
            minLevel = "",
            maxLevel = "",
            limit = "10",
            showRiftMobs = false,
            useAynixPrices = false,
            isRefreshing = false,
        )
    }
}

internal fun ExploreScreenState.toMonsterQueryParams() = MonsterQueryParams(
    minLevel = minLevel.toIntOrNull() ?: 1,
    maxLevel = maxLevel.toIntOrNull() ?: 85,
    chronicle = chronicle,
    limit = limit.toIntOrNull() ?: 10,
    includeRift = showRiftMobs,
    useAynixPrices = useAynixPrices
)

internal sealed interface ExploreScreenEvent {
    data class ChronicleChanged(val chronicle: String) : ExploreScreenEvent
    data class MinLevelChanged(val minLevel: String) : ExploreScreenEvent
    data class MaxLevelChanged(val maxLevel: String) : ExploreScreenEvent
    data class LimitChanged(val limit: String) : ExploreScreenEvent
    data class ShowRiftMobsChanged(val showRiftMobs: Boolean) : ExploreScreenEvent
    object Explore : ExploreScreenEvent
}
