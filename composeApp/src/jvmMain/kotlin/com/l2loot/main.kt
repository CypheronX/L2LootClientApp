package com.l2loot

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.l2loot.di.appModule
import com.l2loot.di.initKoin
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.painterResource

fun main() {
    initKoin {
        modules(appModule)
    }
    
    application {
        var spoilLogoPainter by remember {
            mutableStateOf<Painter?>(null)
        }
        val windowRatio = rememberWindowState(size = DpSize(1280.dp, 720.dp))

        LaunchedEffect(Unit) {
            try {
                val spoilLogoBytes = Res.readBytes("files/app_icon/spoil_logo.png")

                if (spoilLogoBytes.isNotEmpty()) {
                    val imageBitmap = spoilLogoBytes.decodeToImageBitmap()
                    spoilLogoPainter = BitmapPainter(imageBitmap)
                }
            } catch (e: Exception) {
                println("Failed to load spoil icon: ${e.message}")
            }
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "L2Loot",
            icon = spoilLogoPainter,
            state = windowRatio
        ) {
            App()
        }
    }
}