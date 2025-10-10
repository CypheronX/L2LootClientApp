package com.l2loot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.l2loot.data.analytics.AnalyticsService
import com.l2loot.data.settings.UserSettingsRepository
import com.l2loot.design.LocalSpacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter
import java.awt.Desktop
import java.net.URI

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
        description = "I'd like to collect some anonymous usage data to help make the app better. " +
                "Nothing personal is ever collected, and you can turn this off anytime in preferences. ",
        onPrimaryAction = onAccept,
        onSecondaryAction = onDecline,
        primaryButtonText = "Allow",
        secondaryButtonText = "Don't Allow",
        dismissible = false
    )
}

/**
 * Support dialog with links to Patreon and Ko-fi
 * Uses Dialog directly since it has a custom content layout with support platform buttons
 */
@Composable
fun SupportDialog(
    onDismiss: () -> Unit,
    analyticsService: AnalyticsService,
    userSettingsRepository: UserSettingsRepository,
    scope: CoroutineScope,
    isReminderAfterSupport: Boolean = false
) {
    var patreonPainter by remember { mutableStateOf<Painter?>(null) }
    var kofiPainter by remember { mutableStateOf<Painter?>(null) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        try {
            val patreonBytes = Res.readBytes("files/svg/patreon-icon.svg")
            val kofiBytes = Res.readBytes("files/svg/kofi_symbol.svg")

            if (patreonBytes.isNotEmpty()) {
                patreonPainter = patreonBytes.decodeToSvgPainter(density)
            }
            if (kofiBytes.isNotEmpty()) {
                kofiPainter = kofiBytes.decodeToSvgPainter(density)
            }
        } catch (e: Exception) {
            println("Failed to load support icons: ${e.message}")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
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
                    text = "Support L2 Loot",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(LocalSpacing.current.space16))

                Text(
                    text = if (isReminderAfterSupport) {
                        "Hey, just a reminder — your support keeps L2 Loot alive and improving ❤️"
                    } else {
                        "Hey, I'm glad you're enjoying L2 Loot! ❤\uFE0F\n " +
                                "Your support keeps the project alive and helps me keep improving it with new features and updates. If you like what I'm building, consider supporting me — it really makes a difference!"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(LocalSpacing.current.space24))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.space8)
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .pointerHoverIcon(PointerIcon.Hand),
                        onClick = {
                            analyticsService.trackSupportLinkClick("patreon", "dialog")
                            scope.launch {
                                userSettingsRepository.updateLastSupportClickDate(System.currentTimeMillis())
                            }
                            try {
                                Desktop.getDesktop().browse(URI("https://patreon.com/Cypheron?utm_medium=unknown&utm_source=join_link&utm_campaign=creatorshare_creator&utm_content=copyLink"))
                            } catch (e: Exception) {
                                println("Failed to open Patreon URL: ${e.message}")
                            }
                        }
                    ) {
                        patreonPainter?.let { icon ->
                            Image(
                                painter = icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                            )
                            Spacer(modifier = Modifier.width(LocalSpacing.current.space8))
                        }
                        Text("Patreon")
                    }

                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .pointerHoverIcon(PointerIcon.Hand),
                        onClick = {
                            analyticsService.trackSupportLinkClick("kofi", "dialog")
                            scope.launch {
                                userSettingsRepository.updateLastSupportClickDate(System.currentTimeMillis())
                            }
                            try {
                                Desktop.getDesktop().browse(URI("https://ko-fi.com/cypheron"))
                            } catch (e: Exception) {
                                println("Failed to open Ko-fi URL: ${e.message}")
                            }
                        }
                    ) {
                        kofiPainter?.let { icon ->
                            Image(
                                painter = icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(LocalSpacing.current.space8))
                        }
                        Text("Ko-fi")
                    }
                }

                Spacer(modifier = Modifier.height(LocalSpacing.current.space16))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
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
                            text = "Maybe Later",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

