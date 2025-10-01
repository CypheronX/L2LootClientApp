package com.l2loot.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.data.MonsterRepository
import com.l2loot.Monsters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val monsterRepository: MonsterRepository
) : ViewModel() {
    
    private val _monsters = MutableStateFlow<List<Monsters>>(emptyList())
    val monsters: StateFlow<List<Monsters>> = _monsters.asStateFlow()
    
    init {
        loadMonsters()
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
