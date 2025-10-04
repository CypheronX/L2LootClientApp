package com.l2loot.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.data.MonsterRepository
import com.l2loot.Monsters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ExploreViewModel(
    private val monsterRepository: MonsterRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreScreenState.initial())
    val state = _state.asStateFlow()
    private val _monsters = MutableStateFlow<List<Monsters>>(emptyList())
    val monsters: StateFlow<List<Monsters>> = _monsters.asStateFlow()

    val chronicleOptions: List<String>
        get() {
            return listOf("c5", "interlude")
        }

    init {
        loadMonsters()
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
                loadMonsters()
            }
        }
    }
    
    private fun loadMonsters() {
        viewModelScope.launch {
            try {
                val monsterList = monsterRepository.getMonstersInLevelRange(30, 39, chronicle = "c5", limit = 30)
                _monsters.value = monsterList
            } catch (e: Exception) {
                println("Error loading monsters: ${e.message}")
                _monsters.value = emptyList()
            }
        }
    }
}
