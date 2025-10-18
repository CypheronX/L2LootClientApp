package com.l2loot.features.setting

import com.l2loot.domain.model.UpdateInfo

internal data class SettingsState(
    val trackUserEvents: Boolean,
    val availableUpdate: UpdateInfo? = null
) {
    companion object {
        fun initial() = SettingsState(
            trackUserEvents = false
        )
    }
}

internal sealed interface SettingsEvent {
    data class SetTracking(val value: Boolean) : SettingsEvent
    data class CheckForUpdates(val currentVersion: String) : SettingsEvent
}