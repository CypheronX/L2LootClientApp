package com.l2loot.features.setting

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.l2loot.BuildConfig
import com.l2loot.design.LocalSpacing
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Desktop
import java.net.URI

@Composable
fun SettingsScreen() {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.state.collectAsState()
    val horizontalScrollState = rememberScrollState()
    
    LaunchedEffect(Unit) {
        viewModel.onEvent(SettingsEvent.CheckForUpdates(BuildConfig.VERSION_NAME))
    }

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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(LocalSpacing.current.space20))

                state.availableUpdate?.let { updateInfo ->
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
                                text = "Version ${updateInfo.version} is now available!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.space8)
                            ) {
                                Button(
                                    modifier = Modifier
                                        .pointerHoverIcon(PointerIcon.Hand),
                                    onClick = {
                                        try {
                                            Desktop.getDesktop().browse(URI(updateInfo.downloadUrl))
                                        } catch (e: Exception) {
                                            println("Failed to open download URL: ${e.message}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Download")
                                }

                                OutlinedButton(
                                    modifier = Modifier
                                        .pointerHoverIcon(PointerIcon.Hand),
                                    onClick = {
                                        try {
                                            Desktop.getDesktop().browse(URI(updateInfo.releaseUrl))
                                        } catch (e: Exception) {
                                            println("Failed to open release URL: ${e.message}")
                                        }
                                    }
                                ) {
                                    Text("Release Notes")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.size(8.dp))
                }

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
                            checked = state.trackUserEvents,
                            onCheckedChange = {
                                viewModel.onEvent(SettingsEvent.SetTracking(it))
                            },
                            modifier = Modifier
                                .pointerHoverIcon(PointerIcon.Hand)
                        )
                    }
                }
            }
        }
    }
}