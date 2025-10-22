package com.l2loot.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.l2loot.Config
import com.l2loot.design.LocalSpacing
import com.l2loot.domain.model.UpdateInfo

enum class L2LootScreens {
    Explore, Sellable, Settings
}

/**
 * App-specific navigation rail component for the main app navigation.
 * Provides navigation between Explore, Sellable, and Settings screens.
 * 
 * @param selectedDestination The currently selected destination index
 * @param onDestinationSelected Callback when a destination is selected
 * @param logoPainter The logo painter for the header
 * @param spoilPainter The spoil icon painter for Explore navigation
 * @param sellablePainter The sellable icon painter for Sellable navigation
 * @param cogPainter The settings icon painter for Settings navigation
 * @param availableUpdate Optional update info to show notification badge
 * @param modifier Modifier for the component
 */
@Composable
fun AppNavigationRail(
    selectedDestination: Int,
    onDestinationSelected: (L2LootScreens) -> Unit,
    logoPainter: Painter?,
    spoilPainter: Painter?,
    sellablePainter: Painter?,
    cogPainter: Painter?,
    availableUpdate: UpdateInfo?,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        header = {
            logoPainter?.let { logo ->
                Column(
                    modifier = Modifier.padding(top = LocalSpacing.current.space36)
                        .padding(horizontal = LocalSpacing.current.space10),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = logo,
                        contentDescription = null,
                        modifier = Modifier.size(95.dp, 37.dp)
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(LocalSpacing.current.space36))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NavigationRailItem(
                    selected = selectedDestination == L2LootScreens.Explore.ordinal,
                    onClick = { onDestinationSelected(L2LootScreens.Explore) },
                    icon = {
                        spoilPainter?.let { spoil ->
                            Image(
                                painter = spoil,
                                contentDescription = null,
                                colorFilter = if (selectedDestination == L2LootScreens.Explore.ordinal)
                                    ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer) else
                                    ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            "Explore Spoil",
                            color = if (selectedDestination == L2LootScreens.Explore.ordinal)
                                MaterialTheme.colorScheme.secondary else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
                Spacer(modifier = Modifier.size(LocalSpacing.current.space10))
                NavigationRailItem(
                    selected = selectedDestination == L2LootScreens.Sellable.ordinal,
                    onClick = { onDestinationSelected(L2LootScreens.Sellable) },
                    icon = {
                        sellablePainter?.let { sellable ->
                            Image(
                                painter = sellable,
                                contentDescription = null,
                                colorFilter = if (selectedDestination == L2LootScreens.Sellable.ordinal)
                                    ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer) else
                                    ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            "Sellable",
                            color = if (selectedDestination == L2LootScreens.Sellable.ordinal)
                                MaterialTheme.colorScheme.secondary else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
                Spacer(modifier = Modifier.size(LocalSpacing.current.space10))
                NavigationRailItem(
                    selected = selectedDestination == L2LootScreens.Settings.ordinal,
                    onClick = { onDestinationSelected(L2LootScreens.Settings) },
                    icon = {
                        Box {
                            cogPainter?.let { cog ->
                                Image(
                                    painter = cog,
                                    contentDescription = null,
                                    colorFilter = if (selectedDestination == L2LootScreens.Settings.ordinal)
                                        ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer) else
                                        ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            if (availableUpdate != null) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-2).dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = MaterialTheme.shapes.small
                                        )
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            "Settings",
                            color = if (selectedDestination == L2LootScreens.Settings.ordinal)
                                MaterialTheme.colorScheme.secondary else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
            }
            
            Text(
                text = "v${Config.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = LocalSpacing.current.space16)
            )
        }
    }
}
