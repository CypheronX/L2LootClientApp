package com.l2loot.data.repository

import com.l2loot.Config
import com.l2loot.L2LootDatabase
import com.l2loot.data.mapper.toDomainModels
import com.l2loot.data.networking.models.ServerItemsResponse
import com.l2loot.data.networking.models.ServersListResponse
import com.l2loot.data.networking.get
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.model.SellableItem
import com.l2loot.domain.model.ServerName
import com.l2loot.domain.repository.SellableRepository
import com.l2loot.domain.util.DataError
import com.l2loot.domain.util.Result
import com.l2loot.domain.util.map
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SellableRepositoryImpl(
    private val database: L2LootDatabase,
    private val httpClient: HttpClient,
    private val logger: LootLogger
) : SellableRepository {

    private val pollingIntervalMs = 3600000L // 1 hour
    
    private var cachedManagedPrices: MutableMap<ServerName, List<SellableItem>?> = mutableMapOf()
    private var cacheTimestamp: Long = 0

    override suspend fun getAllItemsWithPrices(serverName: ServerName): Result<List<SellableItem>, DataError.Local> {
        return withContext(Dispatchers.IO) {
            try {
                val items = database.sellableItemQueries
                    .getAllItemsWithPrices(serverName.serverKey)
                    .executeAsList()
                    .toDomainModels()
                
                Result.Success(items)
            } catch (e: Exception) {
                logger.error("Failed to get items from database", e)
                Result.Failure(DataError.Local.DISK_FULL)
            }
        }
    }

    private data class FetchResult(
        val items: List<SellableItem>,
        val updatedTime: Long?
    )

    override suspend fun fetchManagedPrices(serverName: ServerName, forceRefresh: Boolean): Result<Long?, DataError.Remote> {
        if (!forceRefresh) {
            val now = System.currentTimeMillis()
            
            if (cachedManagedPrices[serverName] != null && (now - cacheTimestamp) < pollingIntervalMs) {
                val remainingMinutes = (pollingIntervalMs - (now - cacheTimestamp)) / 60000
                logger.info("Using cached ${serverName.displayName} prices ($remainingMinutes min until refresh)")
                return Result.Success(null)
            }
        }
        
        return when (val result = fetchItemsFromFirebase(serverName)) {
            is Result.Success -> {
                val fetchResult = result.data
                val items = fetchResult.items
                val timestamp = System.currentTimeMillis()
                val updateTimestamp = fetchResult.updatedTime?.let { it * 1000 } ?: timestamp
                
                cachedManagedPrices[serverName] = items
                cacheTimestamp = timestamp
                
                withContext(Dispatchers.IO) {
                    database.transaction {
                        for (item in items) {
                            database.managedPricesQueries.upsertPrice(
                                item_id = item.itemId,
                                server_name = serverName.serverKey,
                                price = item.managedPrice ?: item.originalPrice,
                                last_updated = updateTimestamp
                            )
                        }
                    }
                }
                
                logger.info("Fetched ${items.size} ${serverName.displayName} prices from Firebase (cached for 1 hour)")
                Result.Success(fetchResult.updatedTime?.let { it * 1000 })
            }
            is Result.Failure -> {
                logger.error("Failed to fetch ${serverName.displayName} prices: ${result.error}")
                Result.Failure(result.error)
            }
        }
    }

    override suspend fun updateItemPrice(itemKey: String, newPrice: Long): Result<Unit, DataError.Local> {
        return withContext(Dispatchers.IO) {
            try {
                database.sellableItemQueries.updatePrice(
                    item_price = newPrice,
                    key = itemKey
                )
                Result.Success(Unit)
            } catch (e: Exception) {
                logger.error("Failed to update item price for key: $itemKey", e)
                Result.Failure(DataError.Local.DISK_FULL)
            }
        }
    }

    override suspend fun getAvailableServers(): Result<List<String>, DataError.Remote> {
        val route = "${Config.SELLABLE_ITEMS_URL}?list=servers"
        
        return httpClient.get<ServersListResponse>(
            route = route
        ).map { response ->
            logger.info("Fetched ${response.servers.size} available servers")
            response.servers
        }
    }

    private suspend fun fetchItemsFromFirebase(serverName: ServerName): Result<FetchResult, DataError.Remote> {
        val route = "${Config.SELLABLE_ITEMS_URL}?server=${serverName.serverKey}"
        
        return httpClient.get<ServerItemsResponse>(
            route = route
        ).map { response ->
            if (response.items.isEmpty()) {
                logger.warn("Firebase returned empty list for server: ${serverName.displayName}")
            } else {
                logger.info("Fetched ${response.items.size} items for server: ${response.server}")
            }
            FetchResult(
                items = response.items.toDomainModels(),
                updatedTime = response.updatedTime
            )
        }
    }
    
    override suspend fun getLastPriceUpdateTime(serverName: ServerName): Long? {
        return withContext(Dispatchers.IO) {
            try {
                database.managedPricesQueries
                    .getLastUpdateTime(serverName.serverKey)
                    .executeAsOneOrNull()
                    ?.MAX
            } catch (e: Exception) {
                logger.error("Failed to get last update time: ${e.message}")
                null
            }
        }
    }
}

