package com.l2loot.features.sellable

import com.l2loot.data.raw_data.SellableItemJson
import com.l2loot.extensions.abbreviationMatch

internal data class SellableScreenState(
    val items: List<SellableItemJson>,
    val loading: Boolean,
    val pricesByAynix: Boolean,
    val prices: Map<String, String>,
    val searchValue: String,
    val error: String?,
) {

    private val allItemsWithoutAdena: List<SellableItemJson>
        get() = items.filter {
            it.key.lowercase() != "adena" && it.name.lowercase() != "adena"
        }

    fun matchesSearch(item: SellableItemJson): Boolean {
        if (searchValue.isBlank()) return true
        return item.name.contains(searchValue, ignoreCase = true) ||
                item.key.contains(searchValue, ignoreCase = true) ||
                item.key.abbreviationMatch(searchValue)
    }

    val firstColumnAllItems: List<SellableItemJson>
        get() {
            val allItems = allItemsWithoutAdena
            val midpoint = (allItems.size + 1) / 2
            return allItems.take(midpoint)
        }

    val secondColumnAllItems: List<SellableItemJson>
        get() {
            val allItems = allItemsWithoutAdena
            val midpoint = (allItems.size + 1) / 2
            return allItems.drop(midpoint)
        }

    val firstColumnItems: List<SellableItemJson>
        get() = firstColumnAllItems.filter { matchesSearch(it) }

    val secondColumnItems: List<SellableItemJson>
        get() = secondColumnAllItems.filter { matchesSearch(it) }

    val filteredItems: List<SellableItemJson>
        get() = firstColumnItems + secondColumnItems

    companion object {
        fun initial() = SellableScreenState(
            items = emptyList(),
            pricesByAynix = false,
            loading = true,
            prices = emptyMap(),
            searchValue = "",
            error = null,
        )
    }
}

internal sealed interface SellableScreenEvent {
    data class PriceChanged(val itemKey: String, val price: String) : SellableScreenEvent
    data class TogglePriceSource(val value: Boolean) : SellableScreenEvent
    data class OnSearch(val value: String) : SellableScreenEvent
}
