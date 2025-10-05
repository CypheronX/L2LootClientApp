package com.l2loot.data.sellable

import com.l2loot.L2LootDatabase
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
    suspend fun getSellableItemsFromDatabase(): List<SellableItemJson>
}

class SellableRepositoryImpl(
    private val database: L2LootDatabase
) : SellableRepository {
    private val httpClient = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }
    private val firebaseUrl = "REDACTED"

    private val pollingIntervalMs = 3600000L

    override fun getSellableItemsFromFirebase(): Flow<List<SellableItemJson>> = flow {
        while (true) {
            try {
                val items = fetchItemsFromFirebase()
                emit(items)
            } catch (e: Exception) {
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

    private suspend fun fetchItemsFromFirebase(): List<SellableItemJson> = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(firebaseUrl))
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
