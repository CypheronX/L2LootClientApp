package com.l2loot.features.sellable

import com.l2loot.data.raw_data.SellableItemJson

internal data class SellableScreenState(
    val items: List<SellableItemJson>,
    val loading: Boolean,
    val pricesByAynix: Boolean,
    val prices: Map<String, String>,
    val searchValue: String,
    val error: String?,
) {

    val searchedResult: List<SellableItemJson>
        get() {
            if (searchValue.isBlank()) {
                return items
            }
            return items.filter {
                it.name.contains(searchValue, ignoreCase = true)
            }
        }

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
