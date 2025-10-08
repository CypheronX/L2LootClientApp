package com.l2loot.features.setting

internal data class SettingsState(
    val trackUserEvents: Boolean
) {
    companion object {
        fun initial() = SettingsState(
            trackUserEvents = false
        )
    }
}

internal sealed interface SettingsEvent {
    data class SetTracking(val value: Boolean) : SettingsEvent
}