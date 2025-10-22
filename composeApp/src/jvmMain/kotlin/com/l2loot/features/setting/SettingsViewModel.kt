package com.l2loot.features.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.domain.firebase.AnalyticsService
import com.l2loot.domain.repository.UpdateCheckerRepository
import com.l2loot.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    val userSettingsRepository: UserSettingsRepository,
    val analyticsService: AnalyticsService,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState.initial())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = userSettingsRepository.getSettings().first()
            _state.update {
                it.copy(
                    trackUserEvents = settings?.trackEvents ?: false
                )
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetTracking -> {
                viewModelScope.launch {
                    userSettingsRepository.updateTrackEvents(event.value)
                    analyticsService.setTrackingEnabled(event.value)

                    _state.update { currentState ->
                        currentState.copy(
                            trackUserEvents = event.value
                        )
                    }
                }
            }
        }
    }
}