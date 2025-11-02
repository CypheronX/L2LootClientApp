package com.l2loot.domain.repository

import com.l2loot.domain.model.SellableItem
import com.l2loot.domain.model.ServerName
import com.l2loot.domain.util.DataError
import com.l2loot.domain.util.Result


interface SellableRepository {

    suspend fun getAllItemsWithPrices(serverName: ServerName): Result<List<SellableItem>, DataError.Local>

    suspend fun updateItemPrice(itemKey: String, newPrice: Long): Result<Unit, DataError.Local>

    suspend fun fetchManagedPrices(serverName: ServerName, forceRefresh: Boolean = false): Result<Unit, DataError.Remote>
    
    suspend fun getAvailableServers(): Result<List<String>, DataError.Remote>
}

