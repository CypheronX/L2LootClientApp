package com.l2loot.features.explore

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.l2loot.Monsters
import com.l2loot.design.LocalSpacing
import com.l2loot.features.explore.components.ExploreForm
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExploreScreen() {
    val viewModel: ExploreViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = LocalSpacing.current.space34,
                start = LocalSpacing.current.space34
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
                    .fillMaxHeight()
                    .verticalScroll(verticalScrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Explore Spoil",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(LocalSpacing.current.space20))

                ExploreForm(
                    chronicle = state.chronicle,
                    chronicleOptions = viewModel.chronicleOptions,
                    minLevel = state.minLevel,
                    maxLevel = state.maxLevel,
                    limit = state.limit,
                    limitOptions = viewModel.limitOptions,
                    showRiftMobs = state.showRiftMobs,
                    onChronicleChange = { viewModel.onEvent(ExploreScreenEvent.ChronicleChanged(it)) },
                    onMinLevelChange = { viewModel.onEvent(ExploreScreenEvent.MinLevelChanged(it)) },
                    onMaxLevelChange = { viewModel.onEvent(ExploreScreenEvent.MaxLevelChanged(it)) },
                    onLimitChange = { viewModel.onEvent(ExploreScreenEvent.LimitChanged(it)) },
                    onShowRiftMobsChange = { viewModel.onEvent(ExploreScreenEvent.ShowRiftMobsChanged(it)) },
                    onSubmit = { viewModel.onEvent(ExploreScreenEvent.Explore) },
                )
            }
        }
    }
}

@Composable
fun MonsterCard(monster: Monsters) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = monster.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Level: ${monster.level} | Exp: ${monster.exp}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Chronicle: ${monster.chronicle} | HP: ${monster.hp_multiplier}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (monster.is_rift == true) {
                Text(
                    text = "RIFT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}