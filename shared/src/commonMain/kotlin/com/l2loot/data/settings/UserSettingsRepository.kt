package com.l2loot.data.settings

import com.l2loot.L2LootDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.map

data class UserSettings(
    val id: Long,
    val userGuid: String,
    val chronicle: String,
    val minLevel: Long?,
    val maxLevel: Long?,
    val limit: Long,
    val showRiftMobs: Boolean,
    val isAynixPrices: Boolean,
    val trackEvents: Boolean,
    val appOpenCount: Long,
    val lastUpdated: Long?,
)

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
    suspend fun updateIsAynixPrices(isAynixPrices: Boolean)
    suspend fun updateUserGuid(guid: String)
    suspend fun updateTrackEvents(trackEvents: Boolean)
    suspend fun incrementAppOpenCount()
    suspend fun initializeDefaults()
}

class UserSettingsRepositoryImpl(
    private val database: L2LootDatabase
) : UserSettingsRepository {

    override fun getSettings(): Flow<UserSettings?> {
        return database.userSettingsQueries
            .getSettings()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map {
                UserSettings(
                    id = it?.id ?: 1,
                    userGuid = it?.user_guid ?: "",
                    chronicle = it?.chronicle ?: "c5",
                    minLevel = it?.min_level,
                    maxLevel = it?.max_level,
                    limit = it?.limit_results ?: 10,
                    showRiftMobs = it?.show_rift_mobs ?: false,
                    isAynixPrices = it?.is_aynix_prices ?: false,
                    trackEvents = it?.track_events ?: true,
                    appOpenCount = it?.app_open_count ?: 0,
                    lastUpdated = it?.last_updated
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
                is_aynix_prices = isAynixPrices,
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

    override suspend fun updateIsAynixPrices(isAynixPrices: Boolean) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            database.userSettingsQueries.updateIsAynixPrices(
                is_aynix_prices = isAynixPrices,
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
}
