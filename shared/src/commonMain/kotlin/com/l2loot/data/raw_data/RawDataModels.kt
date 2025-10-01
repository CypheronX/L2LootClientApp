package com.l2loot.data.raw_data

import kotlinx.serialization.Serializable

@Serializable
data class MonsterJson(
    val id: Long,
    val name: String,
    val level: Long,
    val exp: Long,
    val is_rift: Int,
    val chronicle: String,
    val hp_multiplier: Double
)

@Serializable
data class SellableItemJson(
    val item_id: Long,
    val name: String,
    val price: Long
)

@Serializable
data class DroplistJson(
    val mob_id: Long,
    val item_id: Long,
    val min: Long,
    val max: Long,
    val chance: Long,
    val category: Long,
    val chronicle: String
)

