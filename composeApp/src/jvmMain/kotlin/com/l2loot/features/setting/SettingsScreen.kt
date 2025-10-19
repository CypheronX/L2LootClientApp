package com.l2loot.features.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.l2loot.Config
import com.l2loot.design.LocalSpacing
import com.l2loot.domain.repository.UserSettingsRepository
import kotlinx.coroutines.launch
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Desktop
import java.net.URI

@Composable
fun SettingsScreen() {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.state.collectAsState()
    val horizontalScrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = LocalSpacing.current.space34,
                start = LocalSpacing.current.space34,
                end = LocalSpacing.current.space34,
            )
    ) {
        val minWidth = 930.dp
        val contentWidth = maxOf(minWidth, maxWidth)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
        ) {
            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.space8)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(LocalSpacing.current.space20))

                Row {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.space16)
                    ) {
                        SettingsSection(
                            trackUserEvents = state.trackUserEvents,
                            onTrackUserEventsChange = { viewModel.onEvent(SettingsEvent.SetTracking(it)) }
                        )
                    }

                    Spacer(modifier = Modifier.size(LocalSpacing.current.space34))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.space16)
                    ) {
                        SupportSection(
                            viewModel = viewModel,
                            userSettingsRepository = viewModel.userSettingsRepository
                        )

                        ReportBugSection()
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Special thanks to Tabi and AYNIX for consultation, testing and help during development.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(LocalSpacing.current.space8))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    trackUserEvents: Boolean,
    onTrackUserEventsChange: (Boolean) -> Unit
) {
    Card(
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .padding(LocalSpacing.current.space16)
        ) {
            Text(
                "Share usage data anonymously",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.size(LocalSpacing.current.space16))
            Switch(
                checked = trackUserEvents,
                onCheckedChange = {
                    onTrackUserEventsChange(it)
                },
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon.Hand)
            )
        }
    }
}

@Composable
private fun SupportSection(
    viewModel: SettingsViewModel,
    userSettingsRepository: UserSettingsRepository
) {
    var patreonPainter by remember { mutableStateOf<Painter?>(null) }
    var kofiPainter by remember { mutableStateOf<Painter?>(null) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

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

    Card(
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(LocalSpacing.current.space16),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.space12)
        ) {
            Text(
                text = "Support My Work",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "If you find L2 Loot helpful, consider supporting the project!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.space8)
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand),
                    onClick = {
                        viewModel.analyticsService.trackSupportLinkClick("patreon", "settings")
                        scope.launch {
                            userSettingsRepository.updateLastSupportClickDate(System.currentTimeMillis())
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
                        .pointerHoverIcon(PointerIcon.Hand),
                    onClick = {
                        viewModel.analyticsService.trackSupportLinkClick("kofi", "settings")
                        scope.launch {
                            userSettingsRepository.updateLastSupportClickDate(System.currentTimeMillis())
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
        }
    }
}

@Composable
private fun ReportBugSection() {
    val uriHandler = LocalUriHandler.current

    Card(
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(LocalSpacing.current.space16),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.space12)
        ) {
            Text(
                text = "Report a Bug",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Found an issue? Let me know!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Email
            Text(
                text = "Email: l2lootapp@gmail.com",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Discord link
            val discordText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                ) {
                    append("Discord: ")
                }

                pushStringAnnotation(
                    tag = "URL",
                    annotation = "https://discord.com/users/217003038022434816"
                )
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.secondary,
                        textDecoration = TextDecoration.Underline,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                ) {
                    append("cypheron discord")
                }
                pop()
            }

            ClickableText(
                text = discordText,
                onClick = { offset ->
                    discordText.getStringAnnotations(
                        tag = "URL",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let { annotation ->
                        uriHandler.openUri(annotation.item)
                    }
                },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            )

            // GitHub Issues link
            val githubText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                ) {
                    append("GitHub Issues: ")
                }

                pushStringAnnotation(
                    tag = "URL",
                    annotation = "https://github.com/aleksbalev/L2LootClientAppReleases/issues"
                )
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.secondary,
                        textDecoration = TextDecoration.Underline,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                ) {
                    append("Report here")
                }
                pop()

                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                ) {
                    append(" (preferred)")
                }
            }

            ClickableText(
                text = githubText,
                onClick = { offset ->
                    githubText.getStringAnnotations(
                        tag = "URL",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let { annotation ->
                        uriHandler.openUri(annotation.item)
                    }
                },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            )
        }
    }
}