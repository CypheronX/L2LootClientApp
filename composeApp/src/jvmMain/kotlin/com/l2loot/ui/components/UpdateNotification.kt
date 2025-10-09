package com.l2loot.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.l2loot.data.update.UpdateInfo
import com.l2loot.design.LocalSpacing
import kotlinx.coroutines.delay
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter
import java.awt.Desktop
import java.net.URI

@Composable
fun UpdateNotification(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }
    val density = LocalDensity.current

    var chevronPainter by remember {
        mutableStateOf<Painter?>(null)
    }

    LaunchedEffect(Unit) {
        try {
            val chevronBytes = Res.readBytes("files/svg/chevron.svg")

            if (chevronBytes.isNotEmpty()) {
                chevronPainter = chevronBytes.decodeToSvgPainter(density)
            }
        } catch (e: Exception) {
            println("Failed to load svg icons: ${e.message}")
        }
    }
    
    LaunchedEffect(Unit) {
        delay(10000)
        visible = false
        delay(300)
        onDismiss()
    }
    
    if (visible) {
        Popup(
            alignment = Alignment.BottomEnd,
            properties = PopupProperties(focusable = false),
            onDismissRequest = { }
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Card(
                    modifier = modifier
                        .padding(16.dp)
                        .width(400.dp)
                        .shadow(8.dp, MaterialTheme.shapes.medium)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(LocalSpacing.current.space16)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎉 Update Available",
                                style = MaterialTheme.typography.titleLarge
                            )

                            IconButton(
                                onClick = {
                                    visible = false
                                    onDismiss()
                                },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                            ) {
                                chevronPainter?.let { painter ->
                                    Image(
                                        painter = painter,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(
                                            MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(LocalSpacing.current.space4))

                        Text(
                            text = "Version ${updateInfo.version} is now available!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(LocalSpacing.current.space12))
                        
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
                                    visible = false
                                    onDismiss()
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
            }
        }
    }
}

