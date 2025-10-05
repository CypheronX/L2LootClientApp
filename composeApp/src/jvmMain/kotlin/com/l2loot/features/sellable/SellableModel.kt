package com.l2loot.features.sellable

import com.l2loot.data.raw_data.SellableItemJson

internal data class SellableScreenState(
    val items: List<SellableItemJson>,
    val loading: Boolean,
    val pricesByAynix: Boolean,
    val prices: Map<String, String>,
    val error: String?
) {
    companion object {
        fun initial() = SellableScreenState(
            items = emptyList(),
            pricesByAynix = false,
            loading = true,
            prices = emptyMap(),
            error = null
        )
    }
}

internal sealed interface SellableScreenEvent {
    data class PriceChanged(val itemKey: String, val price: String) : SellableScreenEvent
    data class TogglePriceSource(val value: Boolean) : SellableScreenEvent
}
