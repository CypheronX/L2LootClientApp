package com.l2loot.domain.model

data class UserSettings(
    val id: Long,
    val userGuid: String,
    val chronicle: String,
    val minLevel: Long?,
    val maxLevel: Long?,
    val limit: Long,
    val showRiftMobs: Boolean,
    val isManagedPrices: Boolean,
    val trackEvents: Boolean,
    val appOpenCount: Long,
    val lastUpdated: Long?,
    val lastPromptDate: Long,
    val sessionCountSincePrompt: Long,
    val lastSupportClickDate: Long,
    val hpMultipliers: Set<HPMultiplier>,
    val serverName: ServerName
)