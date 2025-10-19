package com.l2loot.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.l2loot.Config
import com.l2loot.design.LocalSpacing
import com.l2loot.domain.firebase.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter
import java.awt.Desktop
import java.net.URI

/**
 * Support dialog with links to Patreon and Ko-fi
 * Uses Dialog directly since it has a custom content layout with support platform buttons
 */
@Composable
fun SupportDialog(
    onDismiss: () -> Unit,
    analyticsService: AnalyticsService,
    updateSupportClickDate: suspend (Long) -> Unit,
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
            if (Config.IS_DEBUG) {
                println("Failed to load support icons: ${e.message}")
            }
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
                                updateSupportClickDate(System.currentTimeMillis())
                            }
                            try {
                                Desktop.getDesktop().browse(URI("https://patreon.com/Cypheron?utm_medium=unknown&utm_source=join_link&utm_campaign=creatorshare_creator&utm_content=copyLink"))
                            } catch (e: Exception) {
                                if (Config.IS_DEBUG) {
                                    println("Failed to open Patreon URL: ${e.message}")
                                }
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
                                updateSupportClickDate(System.currentTimeMillis())
                            }
                            try {
                                Desktop.getDesktop().browse(URI("https://ko-fi.com/cypheron"))
                            } catch (e: Exception) {
                                if (Config.IS_DEBUG) {
                                    println("Failed to open Ko-fi URL: ${e.message}")
                                }
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