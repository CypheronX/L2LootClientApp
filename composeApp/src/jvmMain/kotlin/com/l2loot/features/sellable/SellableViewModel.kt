package com.l2loot.features.sellable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.data.SellableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SellableViewModel(
    private val sellableRepository: SellableRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SellableScreenState.initial())
    val state = _state.asStateFlow()

    init {
        loadSellableItems()
    }

    fun onEvent(event: SellableScreenEvent) {
        when (event) {
            is SellableScreenEvent.PriceChanged -> {
                updatePrice(event.itemKey, event.price)
            }
            is SellableScreenEvent.TogglePriceSource -> {
                togglePriceSource(event.value)
            }
        }
    }

    private fun loadSellableItems() {
        val usePricesByAynix = _state.value.pricesByAynix
        
        if (usePricesByAynix) {
            loadFromFirebase()
        } else {
            loadFromDatabase()
        }
    }

    private fun loadFromFirebase() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            
            sellableRepository.getSellableItemsFromFirebase()
                .catch { exception ->
                    _state.update { 
                        it.copy(
                            loading = false,
                            error = exception.message ?: "Unknown error occurred"
                        )
                    }
                }
                .collect { items ->
                    val currentItems = _state.value.items
                    if (items != currentItems) {
                        val firebasePrices = items.associate { it.key to it.price.toString() }
                        _state.update { 
                            it.copy(
                                items = items,
                                prices = firebasePrices,
                                loading = false,
                                error = null
                            )
                        }
                    } else if (_state.value.loading) {
                        _state.update {
                            it.copy(
                                loading = false,
                                error = null
                            )
                        }
                    }
                }
        }
    }

    private fun loadFromDatabase() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = true, error = null) }
                
                val items = sellableRepository.getSellableItemsFromDatabase()
                val databasePrices = items.associate { it.key to it.price.toString() }
                
                _state.update { 
                    it.copy(
                        items = items,
                        prices = databasePrices,
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
        if (!_state.value.pricesByAynix) {
            _state.update { currentState ->
                currentState.copy(
                    prices = currentState.prices + (itemKey to newPrice)
                )
            }
        }
    }

    fun togglePriceSource(useAynixPrices: Boolean) {
        _state.update { it.copy(pricesByAynix = useAynixPrices) }
        loadSellableItems()
    }
}
