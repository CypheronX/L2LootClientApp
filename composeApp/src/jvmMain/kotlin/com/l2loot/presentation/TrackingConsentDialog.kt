package com.l2loot.presentation

import androidx.compose.runtime.Composable
import com.l2loot.designsystem.components.GlobalDialog

/**
 * Specific dialog for tracking consent with pre-filled content
 */
@Composable
fun TrackingConsentDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    GlobalDialog(
        title = "Help Me Improve L2Loot",
        description = "I'd like to collect some anonymous usage data to help make the app better. " +
                "Nothing personal is ever collected, and you can turn this off anytime in preferences. ",
        onPrimaryAction = onAccept,
        onSecondaryAction = onDecline,
        primaryButtonText = "Allow",
        secondaryButtonText = "Don't Allow",
        dismissible = false
    )
}