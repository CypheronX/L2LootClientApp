package com.l2loot

import com.l2loot.domain.model.UpdateInfo

data class MainState(
    val isLoading: Boolean = true,
    val startupProgress: Float = 0f,
    val isStartupComplete: Boolean = false,
    val authState: AuthState = AuthState.Loading,
    val showConsentDialog: Boolean = false,
    val shouldShowConsentAfterLoad: Boolean = false,
    val showSupportDialog: Boolean = false,
    val isSupportDialogReminder: Boolean = false,
    val availableUpdate: UpdateInfo? = null,
    val showUpdateNotification: Boolean = false
)