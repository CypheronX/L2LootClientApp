package com.l2loot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.l2loot.design.LocalSpacing

/**
 * A reusable global dialog component that displays centered with a blackened background.
 * Can be used for confirmations, alerts, consents, or any two-action dialog scenarios.
 * 
 * @param title The title of the dialog
 * @param description The description text explaining the dialog content
 * @param onPrimaryAction Callback when user clicks the primary (right) button
 * @param onSecondaryAction Callback when user clicks the secondary (left) button
 * @param primaryButtonText Text for the primary button (default: "Confirm")
 * @param secondaryButtonText Text for the secondary button (default: "Cancel")
 * @param dismissible Whether the dialog can be dismissed by clicking outside or pressing back (default: false)
 */
@Composable
fun GlobalDialog(
    title: String,
    description: String,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    primaryButtonText: String = "Confirm",
    secondaryButtonText: String = "Cancel",
    dismissible: Boolean = false
) {
    Dialog(
        onDismissRequest = { if (dismissible) onSecondaryAction() },
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = dismissible
        )
    ) {
        Surface(
            modifier = Modifier
                .width(480.dp)
                .wrapContentHeight()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(LocalSpacing.current.space28)
                ),
            shape = RoundedCornerShape(LocalSpacing.current.space28),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(LocalSpacing.current.space24),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(LocalSpacing.current.space16))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(LocalSpacing.current.space24))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onSecondaryAction,
                        modifier = Modifier
                            .height(40.dp)
                            .pointerHoverIcon(PointerIcon.Hand),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(
                            vertical = LocalSpacing.current.space10,
                            horizontal = LocalSpacing.current.space16,
                        ),
                    ) {
                        Text(
                            text = secondaryButtonText,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.size(LocalSpacing.current.space8))

                    TextButton(
                        onClick = onPrimaryAction,
                        modifier = Modifier
                            .height(40.dp)
                            .pointerHoverIcon(PointerIcon.Hand),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(
                            vertical = LocalSpacing.current.space10,
                            horizontal = LocalSpacing.current.space16,
                        )
                    ) {
                        Text(
                            text = primaryButtonText,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

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
        description = "I’d like to collect some anonymous usage data to help make the app better. " +
                "Nothing personal is ever collected, and you can turn this off anytime in preferences. ",
        onPrimaryAction = onAccept,
        onSecondaryAction = onDecline,
        primaryButtonText = "Allow",
        secondaryButtonText = "Don't Allow",
        dismissible = false
    )
}

