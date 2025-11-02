package com.l2loot.data.mapper

import com.l2loot.GetAllItemsWithPrices
import com.l2loot.domain.model.SellableItemJson
import com.l2loot.domain.model.SellableItem


fun GetAllItemsWithPrices.toDomainModel(): SellableItem {
    return SellableItem(
        itemId = item_id,
        key = key,
        name = name,
        originalPrice = original_price,
        managedPrice = managed_price
    )
}

fun SellableItemJson.toDomainModel(): SellableItem {
    return SellableItem(
        itemId = item_id,
        key = key,
        name = name,
        originalPrice = 0,
        managedPrice = price
    )
}

@JvmName("dbItemsToDomainModels")
fun List<GetAllItemsWithPrices>.toDomainModels(): List<SellableItem> {
    return map { it.toDomainModel() }
}

@JvmName("jsonItemsToDomainModels")
fun List<SellableItemJson>.toDomainModels(): List<SellableItem> {
    return map { it.toDomainModel() }
}

