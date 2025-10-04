package com.l2loot.features.explore

import com.l2loot.Monsters

internal data class ExploreScreenState(
    val monsters: List<Monsters>,
    val loading: Boolean,
    val chronicle: String,
    val minLevel: String,
    val maxLevel: String,
    val limit: String,
    val showRiftMobs: Boolean,
) {
    companion object {
        fun initial() = ExploreScreenState(
            monsters = emptyList(),
            loading = true,
            chronicle = "c5",
            minLevel = "",
            maxLevel = "",
            limit = "",
            showRiftMobs = false,
        )
    }
}

internal sealed interface ExploreScreenEvent {
    data class ChronicleChanged(val chronicle: String) : ExploreScreenEvent
    data class MinLevelChanged(val minLevel: String) : ExploreScreenEvent
    data class MaxLevelChanged(val maxLevel: String) : ExploreScreenEvent
    data class LimitChanged(val limit: String) : ExploreScreenEvent
    data class ShowRiftMobsChanged(val showRiftMobs: Boolean) : ExploreScreenEvent
    object Explore : ExploreScreenEvent
}
