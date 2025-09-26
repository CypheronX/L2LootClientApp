package com.l2loot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import l2loot.composeapp.generated.resources.Res
import l2loot.composeapp.generated.resources.*

@Composable
fun getBodyFontFamily() = FontFamily(
    Font(Res.font.Lato_Regular, FontWeight.Normal),
    Font(Res.font.Lato_Bold, FontWeight.Bold),
    Font(Res.font.Lato_Light, FontWeight.Light)
)

@Composable
fun getDisplayFontFamily() = FontFamily(
    Font(Res.font.Roboto_Regular, FontWeight.Normal),
    Font(Res.font.Roboto_Bold, FontWeight.Bold),
    Font(Res.font.Roboto_Medium, FontWeight.Medium)
)

private val baseline = Typography()

@Composable
fun getAppTypography(): Typography {
    val bodyFontFamily = getBodyFontFamily()
    val displayFontFamily = getDisplayFontFamily()
    
    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
        titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
        titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
        titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
        bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
        labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
        labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
        labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
    )
}

