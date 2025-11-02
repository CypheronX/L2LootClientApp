package com.l2loot.data.networking.models

import com.l2loot.domain.model.SellableItemJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerItemsResponse(
    val server: String,
    val items: List<SellableItemJson>,
    @SerialName("updated_time")
    val updatedTime: Long? = null
)

@Serializable
data class ServerMetadata(
    val name: String,
    @SerialName("updated_time")
    val updatedTime: Long? = null,
    @SerialName("item_count")
    val itemCount: Int = 0
)

@Serializable
data class ServersListResponse(
    val servers: List<String>,
    val metadata: List<ServerMetadata>? = null
)
