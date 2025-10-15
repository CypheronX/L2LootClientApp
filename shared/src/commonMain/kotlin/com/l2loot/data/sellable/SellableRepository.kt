package com.l2loot.data.sellable

import com.l2loot.BuildConfig
import com.l2loot.Config
import com.l2loot.GetAllItemsWithPrices
import com.l2loot.L2LootDatabase
import com.l2loot.data.firebase.FirebaseAuthService
import com.l2loot.data.raw_data.SellableItemJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface SellableRepository {
    fun getSellableItemsFromFirebase(): Flow<List<SellableItemJson>>
    suspend fun fetchAynixPricesOnce()
    suspend fun getSellableItemsFromDatabase(): List<SellableItemJson>
    suspend fun getAllItemsWithPrices(): List<GetAllItemsWithPrices>
    suspend fun updateItemPrice(itemKey: String, newPrice: Long)
    suspend fun setFirebaseAuthService(authService: FirebaseAuthService?)
}

class SellableRepositoryImpl(
    private val database: L2LootDatabase
) : SellableRepository {
    private val httpClient = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }
    private var firebaseAuthService: FirebaseAuthService? = null

    private val pollingIntervalMs = 3600000L // 1 hour
    
    private var cachedItems: List<SellableItemJson>? = null
    private var cacheTimestamp: Long = 0
    
    override suspend fun setFirebaseAuthService(authService: FirebaseAuthService?) {
        this.firebaseAuthService = authService
    }

    override fun getSellableItemsFromFirebase(): Flow<List<SellableItemJson>> = flow {
        while (true) {
            try {
                val items = fetchItemsFromFirebase()
                emit(items)

                val timestamp = System.currentTimeMillis()
                for (item in items) {
                    database.aynixPricesQueries.upsertPrice(
                        item_id = item.item_id,
                        price = item.price,
                        last_updated = timestamp
                    )
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    println("❌ Failed to update Aynix prices: ${e.message}")
                    e.printStackTrace()
                }
                emit(emptyList())
            }

            delay(pollingIntervalMs)
        }
    }

    override suspend fun getSellableItemsFromDatabase(): List<SellableItemJson> = withContext(Dispatchers.IO) {
        database.sellableItemQueries.selectAll().executeAsList().map { item ->
            SellableItemJson(
                item_id = item.item_id,
                key = item.key,
                name = item.name,
                price = item.item_price
            )
        }
    }

    override suspend fun fetchAynixPricesOnce() {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            if (cachedItems != null && (now - cacheTimestamp) < pollingIntervalMs) {
                if (BuildConfig.DEBUG) {
                    val remainingMinutes = (pollingIntervalMs - (now - cacheTimestamp)) / 60000
                    println("ℹ️ Using cached Aynix prices (${remainingMinutes} min until refresh)")
                }
                return@withContext
            }
            
            try {
                val items = fetchItemsFromFirebase()
                val timestamp = System.currentTimeMillis()
                
                cachedItems = items
                cacheTimestamp = timestamp
                
                database.transaction {
                    for (item in items) {
                        database.aynixPricesQueries.upsertPrice(
                            item_id = item.item_id,
                            price = item.price,
                            last_updated = timestamp
                        )
                    }
                }
                if (BuildConfig.DEBUG) {
                    println("✅ Fetched ${items.size} Aynix prices from Firebase (cached for 1 hour)")
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    println("❌ Failed to fetch Aynix prices: ${e.message}")
                }
                throw e
            }
        }
    }

    override suspend fun getAllItemsWithPrices(): List<GetAllItemsWithPrices> = withContext(Dispatchers.IO) {
        database.sellableItemQueries.getAllItemsWithPrices().executeAsList()
    }

    override suspend fun updateItemPrice(itemKey: String, newPrice: Long) {
        withContext(Dispatchers.IO) {
            database.sellableItemQueries.updatePrice(
                item_price = newPrice,
                key = itemKey
            )
        }
    }

    private suspend fun fetchItemsFromFirebase(): List<SellableItemJson> = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(Config.SELLABLE_ITEMS_URL))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            val body = response.body()
            
            if (body.isNullOrBlank() || body == "null") {
                if (BuildConfig.DEBUG) {
                    println("⚠️ Firebase returned null/empty data for sellable items")
                }
                return@withContext emptyList()
            }
            
            try {
                json.decodeFromString<List<SellableItemJson>>(body)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    println("⚠️ Failed to parse Firebase response: ${e.message}")
                    println("Response body: $body")
                }
                emptyList()
            }
        } else {
            if (BuildConfig.DEBUG) {
                println("⚠️ Firebase request failed with status code: ${response.statusCode()}")
            }
            emptyList()
        }
    }
}
