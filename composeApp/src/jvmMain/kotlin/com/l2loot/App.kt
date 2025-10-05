package com.l2loot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.l2loot.design.LocalSpacing
import org.koin.compose.koinInject
import com.l2loot.data.LoadDbDataRepository
import kotlinx.coroutines.delay

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

    val loadDbDataRepository: LoadDbDataRepository = koinInject()
    
    val isDatabaseEmpty = remember { loadDbDataRepository.isDatabaseEmpty() }
    val dbLoadProgress by loadDbDataRepository.progress.collectAsState()
    
    var startupProgress by remember { mutableStateOf(0f) }
    var isStartupComplete by remember { mutableStateOf(false) }
    
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

    fun isCurrentlyChosen(currentDestination: Int): Boolean {
        return selectedDestination == currentDestination
    }

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

    LaunchedEffect(Unit) {
        if (!isDatabaseEmpty) {
            for (i in 0..100 step 5) {
                startupProgress = i / 100f
                delay(10)
            }
            startupProgress = 1f
            delay(100)
            isStartupComplete = true
        }
    }
    
    LaunchedEffect(Unit) {
        if (isDatabaseEmpty) {
            isStartupComplete = true
            loadDbDataRepository.load()
        }
    }

    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            Scaffold { contentPadding ->
                val isLoading = !isStartupComplete || (isDatabaseEmpty && dbLoadProgress < 1.0f)
                val currentProgress = if (!isStartupComplete) startupProgress else dbLoadProgress
                
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(contentPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { currentProgress },
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
                } else {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val minHeight = 400.dp
                        val contentHeight = maxOf(minHeight, maxHeight)
                        val verticalScrollState = rememberScrollState()

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(verticalScrollState)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(contentHeight)
                                    .padding(contentPadding)
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
                                }
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .offset(y = -(LocalSpacing.current.space36))

                                ) {
                                    NavigationRailItem(
                                        selected = isCurrentlyChosen(L2LootScreens.Explore.ordinal),
                                        onClick = {
                                            navController.navigate(route = Explore)
                                            selectedDestination = L2LootScreens.Explore.ordinal
                                        },
                                        icon = {
                                            spoilPainter?.let { spoil ->
                                                Image(
                                                    painter = spoil, null,
                                                    colorFilter = if (isCurrentlyChosen(L2LootScreens.Explore.ordinal))
                                                        ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer) else
                                                        ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                "Explore Spoil",
                                                color = if (isCurrentlyChosen(L2LootScreens.Explore.ordinal))
                                                    MaterialTheme.colorScheme.secondary else
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        modifier = Modifier
                                            .pointerHoverIcon(PointerIcon.Hand)
                                    )
                                    Spacer(modifier = Modifier.size(LocalSpacing.current.space10))
                                    NavigationRailItem(
                                        selected = isCurrentlyChosen(L2LootScreens.Sellable.ordinal),
                                        onClick = {
                                            navController.navigate(route = Sellable)
                                            selectedDestination = L2LootScreens.Sellable.ordinal
                                        },
                                        icon = {
                                            sellablePainter?.let { sellable ->
                                                Image(
                                                    sellable, null,
                                                    colorFilter = if (isCurrentlyChosen(L2LootScreens.Sellable.ordinal))
                                                        ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer) else
                                                        ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                "Sellable",
                                                color = if (isCurrentlyChosen(L2LootScreens.Sellable.ordinal))
                                                    MaterialTheme.colorScheme.secondary else
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        modifier = Modifier
                                            .pointerHoverIcon(PointerIcon.Hand)
                                    )
                                    }
                                }
                                NavHost(
                                    navController = navController,
                                    startDestination = Explore,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                ) {
                                    composable<Explore> { ExploreScreen() }
                                    composable<Sellable> { SellableScreen() }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}