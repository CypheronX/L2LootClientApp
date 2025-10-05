package com.l2loot.features.sellable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.data.sellable.SellableRepository
import com.l2loot.data.settings.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SellableViewModel(
    private val sellableRepository: SellableRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SellableScreenState.initial())
    val state = _state.asStateFlow()

    init {
        loadSellableItems()
        
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                val newPricesByAynix = settings?.isAynixPrices ?: false
                val oldPricesByAynix = _state.value.pricesByAynix
                
                _state.update { it.copy(pricesByAynix = newPricesByAynix) }
                
                if (newPricesByAynix != oldPricesByAynix) {
                    loadSellableItems()
                }
            }
        }
    }

    fun onEvent(event: SellableScreenEvent) {
        when (event) {
            is SellableScreenEvent.PriceChanged -> {
                updatePrice(event.itemKey, event.price)
            }
            is SellableScreenEvent.TogglePriceSource -> {
                viewModelScope.launch {
                    userSettingsRepository.updateIsAynixPrices(event.value)
                    
                    if (event.value) {
                        _state.update { it.copy(loading = true) }
                        try {
                            sellableRepository.fetchAynixPricesOnce()
                            loadSellableItems()
                        } catch (e: Exception) {
                            println("❌ Failed to fetch Aynix prices: ${e.message}")
                            _state.update { it.copy(loading = false) }
                        }
                    } else {
                        loadSellableItems()
                    }
                }
            }
        }
    }

    private fun loadSellableItems() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = true, error = null) }
                
                val items = sellableRepository.getAllItemsWithPrices()
                val useAynixPrices = _state.value.pricesByAynix
                
                val prices = items.associate { item ->
                    val selectedPrice = if (useAynixPrices && item.aynix_price != null) {
                        item.aynix_price
                    } else {
                        item.original_price
                    }
                    item.key to selectedPrice.toString()
                }
                
                val itemsForUI = items.map { item ->
                    val selectedPrice = if (useAynixPrices && item.aynix_price != null) {
                        item.aynix_price
                    } else {
                        item.original_price
                    }
                    com.l2loot.data.raw_data.SellableItemJson(
                        item_id = item.item_id,
                        key = item.key,
                        name = item.name,
                        price = selectedPrice ?: 0
                    )
                }
                
                _state.update { 
                    it.copy(
                        items = itemsForUI,
                        prices = prices,
                        loading = false,
                        error = null
                    )
                }
            } catch (exception: Exception) {
                _state.update { 
                    it.copy(
                        loading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun updatePrice(itemKey: String, newPrice: String) {
        // Only allow updates when NOT using Aynix prices
        if (!_state.value.pricesByAynix) {
            // Update UI state immediately for responsiveness
            _state.update { currentState ->
                currentState.copy(
                    prices = currentState.prices + (itemKey to newPrice)
                )
            }
            
            // Persist to database
            viewModelScope.launch {
                try {
                    val priceValue = newPrice.toLongOrNull() ?: 0
                    sellableRepository.updateItemPrice(itemKey, priceValue)
                } catch (e: Exception) {
                    println("❌ Failed to update price: ${e.message}")
                }
            }
        }
    }

}
