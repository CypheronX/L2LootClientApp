package com.l2loot.data.repository

import com.l2loot.L2LootDatabase
import com.l2loot.domain.model.HPMultiplier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.l2loot.domain.model.ServerName
import com.l2loot.domain.model.UserSettings
import com.l2loot.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.map

class UserSettingsRepositoryImpl(
    private val database: L2LootDatabase
) : UserSettingsRepository {

    override fun getSettings(): Flow<UserSettings?> {
        return database.userSettingsQueries
            .getSettings()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map {
                val hpMultipliersString = it?.hp_multipliers ?: ""
                val hpMultipliers = if (hpMultipliersString.isNotEmpty()) {
                    hpMultipliersString.split(",")
                        .mapNotNull { name ->
                            try {
                                HPMultiplier.valueOf(name)
                            } catch (e: IllegalArgumentException) {
                                null
                            }
                        }
                        .toSet()
                } else {
                    emptySet()
                }
                
                UserSettings(
                    id = it?.id ?: 1,
                    userGuid = it?.user_guid ?: "",
                    chronicle = it?.chronicle ?: "c5",
                    minLevel = it?.min_level,
                    maxLevel = it?.max_level,
                    limit = it?.limit_results ?: 10,
                    showRiftMobs = it?.show_rift_mobs ?: false,
                    isManagedPrices = it?.is_managed_prices ?: false,
                    trackEvents = it?.track_events ?: true,
                    appOpenCount = it?.app_open_count ?: 0,
                    lastUpdated = it?.last_updated,
                    lastPromptDate = it?.last_prompt_date ?: 0,
                    sessionCountSincePrompt = it?.session_count_since_prompt ?: 0,
                    lastSupportClickDate = it?.last_support_click_date ?: 0,
                    hpMultipliers = hpMultipliers,
                    serverName = ServerName.fromKey(it?.server ?: "") ?: ServerName.DEFAULT
                )
            }
    }

    override suspend fun saveSettings(
        chronicle: String,
        minLevel: Int,
        maxLevel: Int,
        limitResults: Int,
        showRiftMobs: Boolean,
        isAynixPrices: Boolean
    ) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            database.userSettingsQueries.upsertSettings(
                chronicle = chronicle,
                min_level = minLevel.toLong(),
                max_level = maxLevel.toLong(),
                limit_results = limitResults.toLong(),
                show_rift_mobs = showRiftMobs,
                is_managed_prices = isAynixPrices,
                last_updated = timestamp
            )
        }
    }

    override suspend fun updateChronicle(chronicle: String) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            database.userSettingsQueries.updateChronicle(
                chronicle = chronicle,
                last_updated = timestamp
            )
        }
    }

    override suspend fun updateMinLevel(minLevel: Int) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            database.userSettingsQueries.updateMinLevel(
                min_level = minLevel.toLong(),
                last_updated = timestamp
            )
        }
    }

    override suspend fun updateMaxLevel(maxLevel: Int) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            database.userSettingsQueries.updateMaxLevel(
                max_level = maxLevel.toLong(),
                last_updated = timestamp
            )
        }
    }

    override suspend fun updateLevelRange(minLevel: Int, maxLevel: Int) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            database.userSettingsQueries.updateLevelRange(
                min_level = minLevel.toLong(),
                max_level = maxLevel.toLong(),
                last_updated = timestamp
            )
        }
    }

    override suspend fun updateLimit(limit: Int) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            database.userSettingsQueries.updateLimit(
                limit_results = limit.toLong(),
                last_updated = timestamp
            )
        }
    }

    override suspend fun updateShowRiftMobs(showRiftMobs: Boolean) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            database.userSettingsQueries.updateShowRiftMobs(
                show_rift_mobs = showRiftMobs,
                last_updated = timestamp
            )
        }
    }

    override suspend fun updateIsManagedPrices(isManagedPrices: Boolean) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            database.userSettingsQueries.updateIsManagedPrices(
                is_managed_prices = isManagedPrices,
                last_updated = timestamp
            )
        }
    }

    override suspend fun updateUserGuid(guid: String) {
        withContext(Dispatchers.IO) {
            database.userSettingsQueries.updateUserGuid(user_guid = guid)
        }
    }

    override suspend fun updateTrackEvents(trackEvents: Boolean) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            database.userSettingsQueries.updateTrackEvents(
                track_events = trackEvents,
                last_updated = timestamp
            )
        }
    }

    override suspend fun incrementAppOpenCount() {
        withContext(Dispatchers.IO) {
            database.userSettingsQueries.incrementAppOpenCount()
        }
    }

    override suspend fun initializeDefaults() {
        withContext(Dispatchers.IO) {
            database.userSettingsQueries.initializeDefaults()
        }
    }

    override suspend fun updateLastPromptDate(timestamp: Long) {
        withContext(Dispatchers.IO) {
            database.userSettingsQueries.updateLastPromptDate(last_prompt_date = timestamp)
        }
    }

    override suspend fun incrementSessionCountSincePrompt() {
        withContext(Dispatchers.IO) {
            database.userSettingsQueries.incrementSessionCountSincePrompt()
        }
    }

    override suspend fun updateLastSupportClickDate(timestamp: Long) {
        withContext(Dispatchers.IO) {
            database.userSettingsQueries.updateLastSupportClickDate(last_support_click_date = timestamp)
        }
    }

    override suspend fun updateHPMultipliers(multipliers: Set<HPMultiplier>) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            val multipliersString = multipliers.joinToString(",") { it.name }
            database.userSettingsQueries.updateHPMultipliers(
                hp_multipliers = multipliersString,
                last_updated = timestamp
            )
        }
    }

    override suspend fun updateChosenServer(serverName: ServerName) {
        withContext(Dispatchers.IO) {
            database.userSettingsQueries.updateChosenServer(
                server = serverName.serverKey
            )
        }
    }
}
