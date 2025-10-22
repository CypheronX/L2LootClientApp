package com.l2loot.features.sellable

import com.l2loot.domain.model.SellableItem
import com.l2loot.extensions.abbreviationMatch

internal data class SellableScreenState(
    val items: List<SellableItem>,
    val loading: Boolean,
    val pricesByAynix: Boolean,
    val prices: Map<String, String>,
    val searchValue: String,
    val marketOwnersLink: String,
    val error: String?,
) {

    private val allItemsWithoutAdena: List<SellableItem>
        get() = items.filter {
            it.key.lowercase() != "adena" && it.name.lowercase() != "adena"
        }

    fun matchesSearch(item: SellableItem): Boolean {
        if (searchValue.isBlank()) return true

        val searchString = searchValue.trim().lowercase()
        return item.matchesSearchWithAbbreviation(searchString) { key, query ->
            key.abbreviationMatch(query)
        }
    }

    val firstColumnAllItems: List<SellableItem>
        get() {
            val allItems = allItemsWithoutAdena
            val midpoint = (allItems.size + 1) / 2
            return allItems.take(midpoint)
        }

    val secondColumnAllItems: List<SellableItem>
        get() {
            val allItems = allItemsWithoutAdena
            val midpoint = (allItems.size + 1) / 2
            return allItems.drop(midpoint)
        }

    val firstColumnItems: List<SellableItem>
        get() = firstColumnAllItems.filter { matchesSearch(it) }

    val secondColumnItems: List<SellableItem>
        get() = secondColumnAllItems.filter { matchesSearch(it) }

    val filteredItems: List<SellableItem>
        get() = firstColumnItems + secondColumnItems

    companion object {
        fun initial() = SellableScreenState(
            items = emptyList(),
            pricesByAynix = false,
            loading = true,
            prices = emptyMap(),
            searchValue = "",
            marketOwnersLink = "",
            error = null,
        )
    }
}

internal sealed interface SellableScreenEvent {
    data class PriceChanged(val itemKey: String, val price: String) : SellableScreenEvent
    data class TogglePriceSource(val value: Boolean) : SellableScreenEvent
    data class OnSearch(val value: String) : SellableScreenEvent
}
