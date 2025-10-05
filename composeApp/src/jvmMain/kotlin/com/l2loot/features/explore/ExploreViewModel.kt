package com.l2loot.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.data.monsters.MonsterRepository
import com.l2loot.Monsters
import com.l2loot.data.monsters.strategy.MonsterQueryParams
import com.l2loot.data.monsters.strategy.MonsterResult
import com.l2loot.data.settings.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

internal class ExploreViewModel(
    private val monsterRepository: MonsterRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreScreenState.initial())
    val state = _state.asStateFlow()
    
    init {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                _state.update { 
                    it.copy(
                        limit = settings?.limit?.toString() ?: "10",
                        minLevel = settings?.minLevel?.toString() ?: "",
                        maxLevel = settings?.maxLevel?.toString() ?: "",
                        chronicle = settings?.chronicle ?: "c5",
                        showRiftMobs = settings?.showRiftMobs ?: false,
                        useAynixPrices = settings?.isAynixPrices ?: false
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
                val minLevelValue = event.minLevel.toIntOrNull()
                if (minLevelValue != null) {
                    viewModelScope.launch {
                        userSettingsRepository.updateMinLevel(minLevelValue)
                    }
                }
            }
            is ExploreScreenEvent.MaxLevelChanged -> {
                _state.update { it.copy(maxLevel = event.maxLevel) }
                val maxLevelValue = event.maxLevel.toIntOrNull()
                if (maxLevelValue != null) {
                    viewModelScope.launch {
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
