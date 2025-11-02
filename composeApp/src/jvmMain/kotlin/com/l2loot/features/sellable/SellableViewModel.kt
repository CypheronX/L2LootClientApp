package com.l2loot.features.sellable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.model.ServerName
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
                val newManagedPrices = settings?.isManagedPrices ?: false
                val oldManagedPrices = _state.value.managedPrices
                val newServer = settings?.serverName ?: ServerName.DEFAULT
                val oldServer = _state.value.server
                
                _state.update { it.copy(
                    managedPrices = newManagedPrices,
                    server = newServer
                ) }
                
                if (newManagedPrices != oldManagedPrices || newServer != oldServer) {
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
                        val currentServer = _state.value.server
                        when (val result = sellableRepository.fetchManagedPrices(currentServer)) {
                            is Result.Success -> {
                                userSettingsRepository.updateIsManagedPrices(event.value)
                            }
                            is Result.Failure -> {
                                logger.error("Failed to fetch ${currentServer.displayName} prices: ${result.error}")
                                _state.update { it.copy(loading = false, error = "Failed to fetch prices: ${result.error}") }
                            }
                        }
                    } else {
                        userSettingsRepository.updateIsManagedPrices(event.value)
                    }
                }
            }
            is SellableScreenEvent.OnSearch -> {
                onSearch(event.value)
            }
            is SellableScreenEvent.ServerChanged -> {
                onServerChanged(event.server)
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
            val currentServer = _state.value.server
            
            when (val result = sellableRepository.getAllItemsWithPrices(currentServer)) {
                is Result.Success -> {
                    val items = result.data
                    val useManagedPrices = _state.value.managedPrices
                    
                    val prices = items.associate { item ->
                        val selectedPrice = item.getDisplayPrice(useManagedPrices)
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
        if (!_state.value.managedPrices) {
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

    private fun onServerChanged(server: ServerName) {
        _state.update { it.copy(server = server) }

        loadSellableItems()
        
        if (_state.value.managedPrices) {
            viewModelScope.launch {
                userSettingsRepository.updateChosenServer(server)

                _state.update { it.copy(loading = true) }
                when (val result = sellableRepository.fetchManagedPrices(server)) {
                    is Result.Success -> {
                        loadSellableItems()
                    }
                    is Result.Failure -> {
                        logger.error("Failed to fetch ${server.displayName} prices: ${result.error}")
                        _state.update { it.copy(loading = false, error = "Failed to fetch prices: ${result.error}") }
                    }
                }
            }
        }
    }

}
