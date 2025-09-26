package com.l2loot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.l2loot.ui.theme.AppTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.l2loot.features.explore.ExploreScreen
import com.l2loot.features.sellable.SellableScreen
import kotlinx.serialization.Serializable
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.ColorFilter

@Serializable
object Explore
@Serializable
object Sellable

enum class L2LootScreens {
    Explore, Sellable
}


@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    val startDestination = L2LootScreens.Explore
    var selectedDestination by remember { mutableStateOf(startDestination.ordinal) }

    var spoilPainter by remember {
        mutableStateOf<Painter?>(null)
    }
    var sellablePainter by remember {
        mutableStateOf<Painter?>(null)
    }
    var logoPainter by remember {
        mutableStateOf<Painter?>(null)
    }

    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        try {
            val spoilBytes = Res.readBytes("files/svg/spoil.svg")
            val sellableBytes = Res.readBytes("files/svg/sellable.svg")
            val logoBytes = Res.readBytes("files/svg/l2loot_logo.svg")

            if (spoilBytes.isNotEmpty()) {
                spoilPainter = spoilBytes.decodeToSvgPainter(density)
            }
            if (sellableBytes.isNotEmpty()) {
                sellablePainter = sellableBytes.decodeToSvgPainter(density)
            }
            if (logoBytes.isNotEmpty()) {
                logoPainter = logoBytes.decodeToSvgPainter(density)
            }
        } catch (e: Exception) {
            println("Failed to load svg icons: ${e.message}")
        }
    }

    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            Scaffold { contentPadding ->
                NavigationRail(
                    modifier = Modifier
                        .padding(contentPadding),
                    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    header = {
                        logoPainter?.let { logo ->
                            Column(
                                modifier = Modifier.padding(top = 35.dp)
                                    .padding(horizontal = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = logo, 
                                    contentDescription = null,
                                    modifier = Modifier.size(95.dp, 37.dp)
                                )
                            }
                        }
                    }
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                    ) {
                        NavigationRailItem(
                            selected = selectedDestination == 0,
                            onClick = {
                                navController.navigate(route = Explore)
                                selectedDestination = L2LootScreens.Explore.ordinal
                            },
                            icon = {
                                spoilPainter?.let { spoil ->
                                    Image(painter = spoil, null,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                        modifier = Modifier
                                            .size(24.dp))
                                }
                            },
                            label = {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Explore Spoil",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selectedDestination == 0)
                                            MaterialTheme.colorScheme.secondary else
                                            MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            modifier = Modifier.width(100.dp)
                                .padding(bottom = 10.dp)
                        )
                        NavigationRailItem(
                            selected = selectedDestination == 1,
                            onClick = {
                                navController.navigate(route = Sellable)
                                selectedDestination = L2LootScreens.Sellable.ordinal
                            },
                            icon = {
                                sellablePainter?.let { sellable ->
                                    Image(sellable, null,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                        modifier = Modifier
                                            .size(24.dp))
                                }
                            },
                            label = {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Sellable",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selectedDestination == 1)
                                            MaterialTheme.colorScheme.secondary else
                                            MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
                NavHost(navController = navController, startDestination = Explore) {
                    composable<Explore> { ExploreScreen() }
                    composable<Sellable> { SellableScreen() }
                }
            }

        }
    }
}