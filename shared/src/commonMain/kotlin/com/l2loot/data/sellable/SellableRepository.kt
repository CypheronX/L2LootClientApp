package com.l2loot.data.sellable

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
    private val firebaseUrl = "REDACTED"
    
    private var firebaseAuthService: FirebaseAuthService? = null

    private val pollingIntervalMs = 3600000L
    
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
                println("❌ Failed to update Aynix prices: ${e.message}")
                e.printStackTrace()
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
            try {
                val items = fetchItemsFromFirebase()
                val timestamp = System.currentTimeMillis()
                
                database.transaction {
                    for (item in items) {
                        database.aynixPricesQueries.upsertPrice(
                            item_id = item.item_id,
                            price = item.price,
                            last_updated = timestamp
                        )
                    }
                }
                println("✅ Fetched ${items.size} Aynix prices from Firebase")
            } catch (e: Exception) {
                println("❌ Failed to fetch Aynix prices: ${e.message}")
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
        val idToken = firebaseAuthService?.getIdToken()
        
        val urlWithAuth = if (idToken != null) {
            "$firebaseUrl?auth=$idToken"
        } else {
            firebaseUrl
        }
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create(urlWithAuth))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            val body = response.body()
            
            if (body.isNullOrBlank() || body == "null") {
                println("⚠️ Firebase returned null/empty data for sellable items")
                return@withContext emptyList()
            }
            
            try {
                json.decodeFromString<List<SellableItemJson>>(body)
            } catch (e: Exception) {
                println("⚠️ Failed to parse Firebase response: ${e.message}")
                println("Response body: $body")
                emptyList()
            }
        } else {
            println("⚠️ Firebase request failed with status code: ${response.statusCode()}")
            emptyList()
        }
    }
}
