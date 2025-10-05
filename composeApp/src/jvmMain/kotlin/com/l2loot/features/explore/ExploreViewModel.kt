package com.l2loot.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.data.monsters.MonsterRepository
import com.l2loot.Monsters
import com.l2loot.data.monsters.strategy.MonsterQueryParams
import com.l2loot.data.monsters.strategy.MonsterResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ExploreViewModel(
    private val monsterRepository: MonsterRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreScreenState.initial())
    val state = _state.asStateFlow()

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
            }
            is ExploreScreenEvent.MinLevelChanged -> {
                _state.update { it.copy(minLevel = event.minLevel) }
            }
            is ExploreScreenEvent.MaxLevelChanged -> {
                _state.update { it.copy(maxLevel = event.maxLevel) }
            }
            is ExploreScreenEvent.LimitChanged -> {
                _state.update { it.copy(limit = event.limit) }
            }
            is ExploreScreenEvent.ShowRiftMobsChanged -> {
                _state.update { it.copy(showRiftMobs = event.showRiftMobs) }
            }
            is ExploreScreenEvent.Explore -> {
                loadMonsters(_state.value.toMonsterQueryParams())
            }
        }
    }
    
    private fun loadMonsters(params: MonsterQueryParams) {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }

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

            _state.update { it.copy(isRefreshing = false) }
        }
    }

    fun refresh(params: MonsterQueryParams) {
        loadMonsters(params)
    }
}
