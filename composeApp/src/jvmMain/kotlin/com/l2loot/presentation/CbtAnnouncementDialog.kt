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
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.l2loot.Config
import com.l2loot.design.LocalSpacing
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToImageBitmap

@Composable
fun CbtAnnouncementDialog(
    onDismiss: () -> Unit,
    onJoinDiscord: () -> Unit,
    onRegister: () -> Unit
) {
    var logoPainter by remember { mutableStateOf<Painter?>(null) }

    LaunchedEffect(Unit) {
        try {
            val logoBytes = Res.readBytes("files/pictures/genesis_logo.png")
            if (logoBytes.isNotEmpty()) {
                logoPainter = BitmapPainter(logoBytes.decodeToImageBitmap())
            }
        } catch (e: Exception) {
            if (Config.IS_DEBUG) {
                println("Failed to load genesis logo: ${e.message}")
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
                logoPainter?.let { painter ->
                    Image(
                        painter = painter,
                        contentDescription = "L2Genesis logo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                    Spacer(modifier = Modifier.height(LocalSpacing.current.space16))
                }

                Text(
                    text = "L2Genesis Closed Beta \u2014 Exclusive Rewards Await",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(LocalSpacing.current.space16))

                val bold = SpanStyle(fontWeight = FontWeight.Bold)
                Text(
                    text = buildAnnotatedString {
                        append("An Interlude Classic server built around one belief: every player matters. ")
                        withStyle(bold) { append("No pay-to-win, no shortcuts") }
                        append(" \u2014 just clean gameplay with quality-of-life improvements.\n\n")
                        append("  - ")
                        withStyle(bold) { append("x4 rates") }
                        append("\n  - ")
                        withStyle(bold) { append("Player buff trade shop") }
                        append("\n  - ")
                        withStyle(bold) { append("Crystallization shop") }
                        append("\n  - ")
                        withStyle(bold) { append("Arena mode for FUN PvP\n\n") }
                        append("Closed Beta registration is open for ")
                        withStyle(bold) { append("one week only") }
                        append(". Test with us and earn launch rewards \u2014 ")
                        withStyle(bold) { append("exclusive cosmetics, VIP status, and more") }
                        append(". Every confirmed bug you report earns ")
                        withStyle(bold) { append("Genesis Coins") }
                        append(".\n\nJoin our Discord or register directly below.")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(LocalSpacing.current.space24))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onJoinDiscord,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand),
                    ) {
                        Text("Join Discord")
                    }

                    Spacer(modifier = Modifier.width(LocalSpacing.current.space8))

                    OutlinedButton(
                        onClick = onRegister,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand),
                    ) {
                        Text("Register")
                    }
                }
            }
        }
    }
}
