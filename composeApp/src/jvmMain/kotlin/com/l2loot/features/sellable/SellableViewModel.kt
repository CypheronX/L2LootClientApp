package com.l2loot.features.sellable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.model.ExternalLinks
import com.l2loot.domain.repository.ExternalLinksRepository
import com.l2loot.domain.repository.SellableRepository
import com.l2loot.domain.repository.UserSettingsRepository
import com.l2loot.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

internal class SellableViewModel(
    private val sellableRepository: SellableRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val externalLinksRepository: ExternalLinksRepository,
    private val logger: LootLogger
) : ViewModel() {

    private val _state = MutableStateFlow(SellableScreenState.initial())
    val state = _state.asStateFlow()
    
    private var priceUpdateJob: Job? = null
    private val priceUpdateDebounceMs = 500L

    init {
        loadExternalLinks()
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
                    if (event.value) {
                        _state.update { it.copy(loading = true) }
                        when (val result = sellableRepository.fetchAynixPrices()) {
                            is Result.Success -> {
                                userSettingsRepository.updateIsAynixPrices(event.value)
                            }
                            is Result.Failure -> {
                                logger.error("Failed to fetch Aynix prices: ${result.error}")
                                _state.update { it.copy(loading = false) }
                            }
                        }
                    } else {
                        userSettingsRepository.updateIsAynixPrices(event.value)
                    }
                }
            }
            is SellableScreenEvent.OnSearch -> {
                onSearch(event.value)
            }
        }
    }

    private fun loadExternalLinks() {
        viewModelScope.launch {
            when (val result = externalLinksRepository.fetchExternalLinks()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            marketOwnersLink = result.data.marketOwnersDiscord
                        )
                    }
                }
                is Result.Failure -> {

                }
            }
        }
    }

    private fun loadSellableItems() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            
            val startTime = System.currentTimeMillis()
            
            when (val result = sellableRepository.getAllItemsWithPrices()) {
                is Result.Success -> {
                    val items = result.data
                    val useAynixPrices = _state.value.pricesByAynix
                    
                    val prices = items.associate { item ->
                        val selectedPrice = item.getDisplayPrice(useAynixPrices)
                        item.key to selectedPrice.toString()
                    }
                    
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < 600) {
                        delay(600 - elapsed)
                    }
                    
                    _state.update { 
                        it.copy(
                            items = items,
                            prices = prices,
                            loading = false,
                            error = null
                        )
                    }
                }
                is Result.Failure -> {
                    _state.update { 
                        it.copy(
                            loading = false,
                            error = result.error.toString()
                        )
                    }
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
            
            priceUpdateJob?.cancel()
            priceUpdateJob = viewModelScope.launch {
                delay(priceUpdateDebounceMs)
                val priceValue = newPrice.toLongOrNull() ?: 0
                when (val result = sellableRepository.updateItemPrice(itemKey, priceValue)) {
                    is Result.Success -> {
                        // Price updated successfully
                    }
                    is Result.Failure -> {
                        logger.error("Failed to update price: ${result.error}")
                    }
                }
            }
        }
    }

    private fun onSearch(searchValue: String) {
        _state.update { it.copy(searchValue = searchValue) }
    }

}
