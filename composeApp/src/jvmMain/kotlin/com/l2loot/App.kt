package com.l2loot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.l2loot.designsystem.modifiers.clearFocusOnClick
import com.l2loot.designsystem.theme.AppTheme
import com.l2loot.features.explore.ExploreScreen
import com.l2loot.features.sellable.SellableScreen
import kotlinx.serialization.Serializable
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import com.l2loot.domain.firebase.AnalyticsService
import com.l2loot.features.setting.SettingsScreen
import com.l2loot.presentation.AppDialogManager
import com.l2loot.presentation.AppLoadingScreen
import com.l2loot.presentation.AppNavigationRail
import com.l2loot.presentation.L2LootScreens
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object Explore
@Serializable
object Sellable
@Serializable
object Settings

@Composable
@Preview
fun App(
    viewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()
    val dbLoadProgress by viewModel.dbLoadProgress.collectAsState()

    val startDestination = L2LootScreens.Explore
    var selectedDestination by remember { mutableStateOf(startDestination.ordinal) }

    // Inject dependencies for dialogs
    val analyticsService: AnalyticsService = koinInject()
    val scope = rememberCoroutineScope()
    
    // Load SVG icons
    var spoilPainter by remember { mutableStateOf<Painter?>(null) }
    var sellablePainter by remember { mutableStateOf<Painter?>(null) }
    var logoPainter by remember { mutableStateOf<Painter?>(null) }
    var cogPainter by remember { mutableStateOf<Painter?>(null) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        try {
            val spoilBytes = Res.readBytes("files/svg/spoil.svg")
            val sellableBytes = Res.readBytes("files/svg/sellable.svg")
            val logoBytes = Res.readBytes("files/svg/l2loot_logo.svg")
            val cogBytes = Res.readBytes("files/svg/cog.svg")

            if (spoilBytes.isNotEmpty()) {
                spoilPainter = spoilBytes.decodeToSvgPainter(density)
            }
            if (sellableBytes.isNotEmpty()) {
                sellablePainter = sellableBytes.decodeToSvgPainter(density)
            }
            if (logoBytes.isNotEmpty()) {
                logoPainter = logoBytes.decodeToSvgPainter(density)
            }
            if (cogBytes.isNotEmpty()) {
                cogPainter = cogBytes.decodeToSvgPainter(density)
            }
        } catch (e: Exception) {
            if (Config.IS_DEBUG) {
                println("Failed to load svg icons: ${e.message}")
            }
        }
    }

    // Handle consent dialog
    LaunchedEffect(state.isLoading, state.shouldShowConsentAfterLoad) {
        if (!state.isLoading && state.shouldShowConsentAfterLoad) {
            delay(500)
            viewModel.showConsentDialog()
        }
    }

    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            AppDialogManager(
                showConsentDialog = state.showConsentDialog,
                showSupportDialog = state.showSupportDialog,
                isSupportDialogReminder = state.isSupportDialogReminder,
                analyticsService = analyticsService,
                onAcceptConsent = viewModel::acceptConsent,
                onDeclineConsent = viewModel::declineConsent,
                onDismissSupport = viewModel::hideSupportDialog,
                onUpdateSupportClickDate = viewModel::updateSupportClickDate,
                scope = scope
            )
            
            val currentProgress = if (!state.isStartupComplete) state.startupProgress else dbLoadProgress
            
            Scaffold { contentPadding ->
                if (state.isLoading) {
                    AppLoadingScreen(
                        progress = currentProgress,
                        isLoading = state.isLoading,
                        isStartupComplete = state.isStartupComplete,
                        modifier = Modifier.fillMaxSize().padding(contentPadding)
                    )
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
                                AppNavigationRail(
                                    selectedDestination = selectedDestination,
                                    onDestinationSelected = { screen ->
                                        when (screen) {
                                            L2LootScreens.Explore -> {
                                                navController.navigate(route = Explore)
                                                selectedDestination = screen.ordinal
                                            }
                                            L2LootScreens.Sellable -> {
                                                navController.navigate(route = Sellable)
                                                selectedDestination = screen.ordinal
                                            }
                                            L2LootScreens.Settings -> {
                                                navController.navigate(route = Settings)
                                                selectedDestination = screen.ordinal
                                            }
                                        }
                                    },
                                    logoPainter = logoPainter,
                                    spoilPainter = spoilPainter,
                                    sellablePainter = sellablePainter,
                                    cogPainter = cogPainter,
                                    availableUpdate = state.availableUpdate
                                )
                                
                                NavHost(
                                    navController = navController,
                                    startDestination = Explore,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clearFocusOnClick()
                                ) {
                                    composable<Explore> { ExploreScreen() }
                                    composable<Sellable> { SellableScreen() }
                                    composable<Settings> { SettingsScreen() }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}