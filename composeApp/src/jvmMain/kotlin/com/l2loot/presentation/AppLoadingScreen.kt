package com.l2loot.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.l2loot.design.LocalSpacing

/**
 * App-specific loading screen component that displays progress and status information.
 * Used during app startup or database loading operations.
 * 
 * @param progress The current progress value (0.0 to 1.0)
 * @param isLoading Whether the loading operation is currently active
 * @param isStartupComplete Whether the startup phase is complete
 * @param modifier Modifier for the component
 */
@Composable
fun AppLoadingScreen(
    progress: Float,
    isLoading: Boolean,
    isStartupComplete: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { progress },
            gapSize = LocalSpacing.current.none,
            drawStopIndicator = { },
            modifier = Modifier.padding(horizontal = 32.dp)
                .fillMaxWidth(fraction = 0.6f)
        )
        Spacer(modifier = Modifier.size(LocalSpacing.current.space16))
        Text(
            text = if (!isStartupComplete) "Starting..." else "Loading database...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
