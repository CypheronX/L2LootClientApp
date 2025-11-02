package com.l2loot.domain.repository

import com.l2loot.domain.model.HPMultiplier
import com.l2loot.domain.model.ServerName
import com.l2loot.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getSettings(): Flow<UserSettings?>
    suspend fun saveSettings(
        chronicle: String,
        minLevel: Int,
        maxLevel: Int,
        limitResults: Int,
        showRiftMobs: Boolean,
        isAynixPrices: Boolean
    )
    suspend fun updateChronicle(chronicle: String)
    suspend fun updateMinLevel(minLevel: Int)
    suspend fun updateMaxLevel(maxLevel: Int)
    suspend fun updateLevelRange(minLevel: Int, maxLevel: Int)
    suspend fun updateLimit(limit: Int)
    suspend fun updateShowRiftMobs(showRiftMobs: Boolean)
    suspend fun updateIsManagedPrices(isManagedPrices: Boolean)
    suspend fun updateUserGuid(guid: String)
    suspend fun updateTrackEvents(trackEvents: Boolean)
    suspend fun incrementAppOpenCount()
    suspend fun initializeDefaults()
    suspend fun updateLastPromptDate(timestamp: Long)
    suspend fun incrementSessionCountSincePrompt()
    suspend fun updateLastSupportClickDate(timestamp: Long)
    suspend fun updateHPMultipliers(multipliers: Set<HPMultiplier>)
    suspend fun updateChosenServer(serverName: ServerName)
}