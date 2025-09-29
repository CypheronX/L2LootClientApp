package com.l2loot.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.data.MonsterRepository
import com.l2loot.db.Monsters
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
                val monsterList = monsterRepository.getAllMonsters()
                _monsters.value = monsterList
            } catch (e: Exception) {
                println("Error loading monsters: ${e.message}")
                _monsters.value = emptyList()
            }
        }
    }
}
