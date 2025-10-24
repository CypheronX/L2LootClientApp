package com.l2loot.data.repository

import com.l2loot.Config
import com.l2loot.L2LootDatabase
import com.l2loot.data.mapper.toDomainModels
import com.l2loot.domain.model.SellableItemJson
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.model.SellableItem
import com.l2loot.domain.repository.SellableRepository
import com.l2loot.data.networking.get
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
    
    private var cachedAynixPrices: List<SellableItem>? = null
    private var cacheTimestamp: Long = 0

    override suspend fun getAllItemsWithPrices(): Result<List<SellableItem>, DataError.Local> {
        return withContext(Dispatchers.IO) {
            try {
                val items = database.sellableItemQueries
                    .getAllItemsWithPrices()
                    .executeAsList()
                    .toDomainModels()
                
                Result.Success(items)
            } catch (e: Exception) {
                logger.error("Failed to get items from database", e)
                Result.Failure(DataError.Local.DISK_FULL)
            }
        }
    }

    override suspend fun fetchAynixPrices(forceRefresh: Boolean): Result<Unit, DataError.Remote> {
        if (!forceRefresh) {
            val now = System.currentTimeMillis()
            
            if (cachedAynixPrices != null && (now - cacheTimestamp) < pollingIntervalMs) {
                val remainingMinutes = (pollingIntervalMs - (now - cacheTimestamp)) / 60000
                logger.info("Using cached Aynix prices ($remainingMinutes min until refresh)")
                return Result.Success(Unit)
            }
        }
        
        return when (val result = fetchItemsFromFirebase()) {
            is Result.Success -> {
                val items = result.data
                val timestamp = System.currentTimeMillis()
                
                cachedAynixPrices = items
                cacheTimestamp = timestamp
                
                withContext(Dispatchers.IO) {
                    database.transaction {
                        for (item in items) {
                            database.aynixPricesQueries.upsertPrice(
                                item_id = item.itemId,
                                price = item.aynixPrice ?: item.originalPrice,
                                last_updated = timestamp
                            )
                        }
                    }
                }
                
                logger.info("Fetched ${items.size} Aynix prices from Firebase (cached for 1 hour)")
                Result.Success(Unit)
            }
            is Result.Failure -> {
                logger.error("Failed to fetch Aynix prices: ${result.error}")
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

    private suspend fun fetchItemsFromFirebase(): Result<List<SellableItem>, DataError.Remote> {
        return httpClient.get<List<SellableItemJson>>(
            route = Config.SELLABLE_ITEMS_URL
        ).map { jsonItems ->
            if (jsonItems.isEmpty()) {
                logger.warn("Firebase returned empty list for sellable items")
            }
            jsonItems.toDomainModels()
        }
    }
}

