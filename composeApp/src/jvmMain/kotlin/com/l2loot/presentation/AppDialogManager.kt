package com.l2loot.presentation

import androidx.compose.runtime.Composable
import com.l2loot.domain.firebase.AnalyticsService
import com.l2loot.domain.model.UpdateInfo
import kotlinx.coroutines.CoroutineScope

/**
 * Manages all app-specific dialogs and notifications.
 * This component centralizes dialog state management for the main app screen.
 */
@Composable
fun AppDialogManager(
    showConsentDialog: Boolean,
    showSupportDialog: Boolean,
    isSupportDialogReminder: Boolean,
    showUpdateNotification: Boolean,
    availableUpdate: UpdateInfo?,
    analyticsService: AnalyticsService,
    onAcceptConsent: () -> Unit,
    onDeclineConsent: () -> Unit,
    onDismissSupport: () -> Unit,
    onDismissUpdate: () -> Unit,
    onUpdateSupportClickDate: (Long) -> Unit,
    scope: CoroutineScope
) {
    if (showConsentDialog) {
        TrackingConsentDialog(
            onAccept = onAcceptConsent,
            onDecline = onDeclineConsent
        )
    }
    
    if (showSupportDialog) {
        SupportDialog(
            onDismiss = onDismissSupport,
            analyticsService = analyticsService,
            updateSupportClickDate = onUpdateSupportClickDate,
            scope = scope,
            isReminderAfterSupport = isSupportDialogReminder
        )
    }
    
    if (showUpdateNotification && availableUpdate != null) {
        UpdateNotification(
            updateInfo = availableUpdate,
            onDismiss = onDismissUpdate
        )
    }
}
