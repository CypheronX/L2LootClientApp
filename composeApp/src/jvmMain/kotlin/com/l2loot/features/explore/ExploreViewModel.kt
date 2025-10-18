package com.l2loot.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.domain.model.MonsterQueryParams
import com.l2loot.domain.repository.MonsterRepository
import com.l2loot.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

internal class ExploreViewModel(
    private val monsterRepository: MonsterRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreScreenState.initial())
    val state = _state.asStateFlow()
    
    private var minLevelUpdateJob: Job? = null
    private var maxLevelUpdateJob: Job? = null
    private val inputDebounceMs = 500L
    
    init {
        viewModelScope.launch {
            val initialSettings = userSettingsRepository.getSettings().first()
            _state.update { 
                it.copy(
                    limit = initialSettings?.limit?.toString() ?: "10",
                    minLevel = initialSettings?.minLevel?.toString() ?: "",
                    maxLevel = initialSettings?.maxLevel?.toString() ?: "",
                    chronicle = initialSettings?.chronicle ?: "c5",
                    showRiftMobs = initialSettings?.showRiftMobs ?: false,
                    useAynixPrices = initialSettings?.isAynixPrices ?: false,
                    selectedHPMultipliers = initialSettings?.hpMultipliers ?: emptySet()
                )
            }
            
            loadMonsters(_state.value.toMonsterQueryParams())
            
            userSettingsRepository.getSettings().collect { settings ->
                _state.update { 
                    it.copy(
                        limit = settings?.limit?.toString() ?: "10",
                        minLevel = settings?.minLevel?.toString() ?: "",
                        maxLevel = settings?.maxLevel?.toString() ?: "",
                        chronicle = settings?.chronicle ?: "c5",
                        showRiftMobs = settings?.showRiftMobs ?: false,
                        useAynixPrices = settings?.isAynixPrices ?: false,
                        selectedHPMultipliers = settings?.hpMultipliers ?: emptySet()
                    )
                }
            }
        }
    }

    val chronicleOptions: List<String>
        get() {
            return listOf("c5", "interlude")
        }
    val limitOptions: List<String>
        get() {
            return listOf("10", "30", "60", "100")
        }

    fun onEvent(event: ExploreScreenEvent) {
        when (event) {
            is ExploreScreenEvent.ChronicleChanged -> {
                _state.update { it.copy(chronicle = event.chronicle) }
                viewModelScope.launch {
                    userSettingsRepository.updateChronicle(event.chronicle)
                }
            }
            is ExploreScreenEvent.MinLevelChanged -> {
                _state.update { it.copy(minLevel = event.minLevel) }
                
                minLevelUpdateJob?.cancel()
                val minLevelValue = event.minLevel.toIntOrNull()
                if (minLevelValue != null) {
                    minLevelUpdateJob = viewModelScope.launch {
                        delay(inputDebounceMs)
                        userSettingsRepository.updateMinLevel(minLevelValue)
                    }
                }
            }
            is ExploreScreenEvent.MaxLevelChanged -> {
                _state.update { it.copy(maxLevel = event.maxLevel) }
                
                maxLevelUpdateJob?.cancel()
                val maxLevelValue = event.maxLevel.toIntOrNull()
                if (maxLevelValue != null) {
                    maxLevelUpdateJob = viewModelScope.launch {
                        delay(inputDebounceMs)
                        userSettingsRepository.updateMaxLevel(maxLevelValue)
                    }
                }
            }
            is ExploreScreenEvent.LimitChanged -> {
                _state.update { it.copy(limit = event.limit) }
                viewModelScope.launch {
                    val limitInt = event.limit.toIntOrNull() ?: 10
                    userSettingsRepository.updateLimit(limitInt)
                }
            }
            is ExploreScreenEvent.ShowRiftMobsChanged -> {
                _state.update { it.copy(showRiftMobs = event.showRiftMobs) }
                viewModelScope.launch {
                    userSettingsRepository.updateShowRiftMobs(event.showRiftMobs)
                }
            }
            is ExploreScreenEvent.HPMultiplierToggled -> {
                _state.update { currentState ->
                    val newMultipliers = if (event.multiplier in currentState.selectedHPMultipliers) {
                        currentState.selectedHPMultipliers - event.multiplier
                    } else {
                        currentState.selectedHPMultipliers + event.multiplier
                    }
                    currentState.copy(selectedHPMultipliers = newMultipliers)
                }
                viewModelScope.launch {
                    userSettingsRepository.updateHPMultipliers(_state.value.selectedHPMultipliers)
                }
                loadMonsters(_state.value.toMonsterQueryParams())
            }
            is ExploreScreenEvent.Explore -> {
                loadMonsters(_state.value.toMonsterQueryParams())
            }
        }
    }
    
    private fun loadMonsters(params: MonsterQueryParams) {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            
            val startTime = System.currentTimeMillis()

            monsterRepository.getMonsters(params)
                .first()
                .fold(
                    onSuccess = {
                        _state.update { currentState ->
                            currentState.copy(
                                monsters = it
                            )
                        }
                    },
                    onFailure = {}
                )

            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < 800) {
                delay(800 - elapsed)
            }

            _state.update { it.copy(isRefreshing = false) }
        }
    }

    fun refresh(params: MonsterQueryParams) {
        loadMonsters(params)
    }
}
