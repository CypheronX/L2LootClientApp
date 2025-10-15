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
import androidx.compose.ui.text.font.FontVariation
import com.l2loot.design.LocalSpacing
import org.koin.compose.koinInject
import com.l2loot.data.LoadDbDataRepository
import com.l2loot.data.analytics.AnalyticsService
import com.l2loot.data.analytics.generateUserGuid
import com.l2loot.data.firebase.FirebaseAuthService
import com.l2loot.data.sellable.SellableRepository
import com.l2loot.data.settings.UserSettings
import com.l2loot.data.settings.UserSettingsRepository
import com.l2loot.data.update.UpdateChecker
import com.l2loot.data.update.UpdateInfo
import com.l2loot.features.setting.SettingsScreen
import com.l2loot.ui.components.SupportDialog
import com.l2loot.ui.components.TrackingConsentDialog
import com.l2loot.ui.components.UpdateNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Serializable
object Explore
@Serializable
object Sellable
@Serializable
object Settings

enum class L2LootScreens {
    Explore, Sellable, Settings
}

sealed class AuthState {
    object Loading : AuthState()
    object Success : AuthState()
    object Failed : AuthState()
}


@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    val startDestination = L2LootScreens.Explore
    var selectedDestination by remember { mutableStateOf(startDestination.ordinal) }

    val loadDbDataRepository: LoadDbDataRepository = koinInject()
    val sellableRepository: SellableRepository = koinInject()
    val userSettingsRepository: UserSettingsRepository = koinInject()
    val analyticsService: AnalyticsService = koinInject()
    val updateChecker: UpdateChecker = koinInject()
    val firebaseAuthService: FirebaseAuthService = koinInject()
    
    val scope = rememberCoroutineScope()
    val isDatabaseEmpty = remember { loadDbDataRepository.isDatabaseEmpty() }
    val dbLoadProgress by loadDbDataRepository.progress.collectAsState()
    var isLoading: Boolean = true

    var startupProgress by remember { mutableStateOf(0f) }
    var isStartupComplete by remember { mutableStateOf(false) }
    var showConsentDialog by remember { mutableStateOf(false) }
    var shouldShowConsentAfterLoad by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var isSupportDialogReminder by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }
    var showUpdateNotification by remember { mutableStateOf(false) }
    var authState by remember { mutableStateOf<AuthState>(AuthState.Loading) }
    
    var spoilPainter by remember {
        mutableStateOf<Painter?>(null)
    }
    var sellablePainter by remember {
        mutableStateOf<Painter?>(null)
    }
    var logoPainter by remember {
        mutableStateOf<Painter?>(null)
    }
    var cogPainter by remember {
        mutableStateOf<Painter?>(null)
    }

    val density = LocalDensity.current

    fun isCurrentlyChosen(currentDestination: Int): Boolean {
        return selectedDestination == currentDestination
    }

    fun shouldShowSupportDialog(settings: UserSettings?): Pair<Boolean, Boolean> {
        if (settings == null) return Pair(false, false)

        val currentTime = System.currentTimeMillis()
        val sessionCount = settings.sessionCountSincePrompt
        val lastPromptDate = settings.lastPromptDate
        val lastSupportClickDate = settings.lastSupportClickDate

        val dayInMillis = 24 * 60 * 60 * 1000L
        val weekInMillis = 7 * dayInMillis
        val twoWeeksInMillis = 14 * dayInMillis
        val threeMonthsInMillis = 90 * dayInMillis
 
        if (lastPromptDate > 0 && (currentTime - lastPromptDate) < weekInMillis) {
            return Pair(false, false)
        }

        if (lastSupportClickDate > 0) {
            val timeSinceClick = currentTime - lastSupportClickDate

            if (timeSinceClick < threeMonthsInMillis) {
                return Pair(false, false)
            }

            if (lastPromptDate <= lastSupportClickDate) {
                return Pair(true, true)
            }

            return Pair(false, false)
        }

        if (lastPromptDate == 0L) {
            return Pair(sessionCount >= 3, false)
        }

        val timeSinceLastPrompt = currentTime - lastPromptDate
        return Pair(timeSinceLastPrompt >= twoWeeksInMillis, false)
    }

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
            if (BuildConfig.DEBUG) {
                println("Failed to load svg icons: ${e.message}")
            }
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
    
    LaunchedEffect(Unit) {
        userSettingsRepository.initializeDefaults()
    }
    
    LaunchedEffect(Unit) {
        val token = firebaseAuthService.getIdToken()
        sellableRepository.setFirebaseAuthService(firebaseAuthService)
        authState = if (token != null) AuthState.Success else AuthState.Failed
        if (token == null && BuildConfig.DEBUG) {
            println("⚠️ Firebase authentication failed - some features may be unavailable")
        }
    }
    
    LaunchedEffect(Unit) {
        val settings = userSettingsRepository.getSettings().firstOrNull()
        
        val isFirstOpen = settings?.userGuid.isNullOrEmpty()
        
        if (isFirstOpen) {
            val newGuid = generateUserGuid()
            analyticsService.setUserGuid(newGuid)
            analyticsService.setTrackingEnabled(true)
            
            userSettingsRepository.updateUserGuid(newGuid)
            userSettingsRepository.updateTrackEvents(true)
            
            analyticsService.trackAppOpen(isFirstOpen = true)
            
            shouldShowConsentAfterLoad = true
        } else {
            analyticsService.setUserGuid(settings.userGuid)
            analyticsService.setTrackingEnabled(settings.trackEvents)
            analyticsService.trackAppOpen(isFirstOpen = false)
        }
        
        userSettingsRepository.incrementAppOpenCount()
        userSettingsRepository.incrementSessionCountSincePrompt()
        
        val (shouldShowSupport, isReminder) = shouldShowSupportDialog(settings)
        
        if (shouldShowSupport && !isFirstOpen) {
            while (isLoading || showConsentDialog) {
                delay(100)
            }
            delay(500)
            showSupportDialog = true
            isSupportDialogReminder = isReminder
            
            userSettingsRepository.updateLastPromptDate(System.currentTimeMillis())
        }
    }
    
    LaunchedEffect(authState) {
        // Only fetch data if authentication succeeded
        if (authState == AuthState.Success) {
            userSettingsRepository.getSettings()
                .collectLatest { settings ->
                    if (settings?.isAynixPrices == true) {
                        sellableRepository.getSellableItemsFromFirebase().collect()
                    }
                }
        } else if (authState == AuthState.Failed && BuildConfig.DEBUG) {
            println("⚠️ Skipping Firebase data fetch due to authentication failure")
        }
    }
    
    // Check for updates on startup
    LaunchedEffect(Unit) {
        delay(2000) // Wait 2 seconds after startup
        try {
            val updateInfo = updateChecker.checkForUpdate(BuildConfig.VERSION_NAME)
            if (updateInfo != null) {
                availableUpdate = updateInfo
                showUpdateNotification = true
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                println("Failed to check for updates: ${e.message}")
            }
        }
    }

    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            isLoading = !isStartupComplete || (isDatabaseEmpty && dbLoadProgress < 1.0f)
            
            LaunchedEffect(isLoading, shouldShowConsentAfterLoad) {
                if (!isLoading && shouldShowConsentAfterLoad) {
                    delay(500)
                    showConsentDialog = true
                }
            }
            
            if (showConsentDialog) {
                TrackingConsentDialog(
                    onAccept = {
                        showConsentDialog = false
                        scope.launch {
                            val currentGuid = analyticsService.getUserGuid()
                            userSettingsRepository.updateUserGuid(currentGuid)
                            userSettingsRepository.updateTrackEvents(trackEvents = true)
                            analyticsService.setTrackingEnabled(true)
                        }
                    },
                    onDecline = {
                        showConsentDialog = false
                        scope.launch {
                            val currentGuid = analyticsService.getUserGuid()
                            userSettingsRepository.updateUserGuid(currentGuid)
                            userSettingsRepository.updateTrackEvents(trackEvents = false)
                            analyticsService.setTrackingEnabled(false)
                        }
                    }
                )
            }
            
            if (showSupportDialog) {
                SupportDialog(
                    onDismiss = { showSupportDialog = false },
                    analyticsService = analyticsService,
                    userSettingsRepository = userSettingsRepository,
                    scope = scope,
                    isReminderAfterSupport = isSupportDialogReminder
                )
            }
            
            if (showUpdateNotification && availableUpdate != null) {
                UpdateNotification(
                    updateInfo = availableUpdate!!,
                    onDismiss = { showUpdateNotification = false }
                )
            }
            
            val currentProgress = if (!isStartupComplete) startupProgress else dbLoadProgress
            
            Scaffold { contentPadding ->
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
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxHeight()

                                ) {
                                    Spacer(modifier = Modifier.height(LocalSpacing.current.space36))
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
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
                                        Spacer(modifier = Modifier.size(LocalSpacing.current.space10))
                                        NavigationRailItem(
                                            selected = isCurrentlyChosen(L2LootScreens.Settings.ordinal),
                                            onClick = {
                                                navController.navigate(route = Settings)
                                                selectedDestination = L2LootScreens.Settings.ordinal
                                            },
                                            icon = {
                                                Box {
                                                    cogPainter?.let { cog ->
                                                        Image(
                                                            cog, null,
                                                            colorFilter = if (isCurrentlyChosen(L2LootScreens.Settings.ordinal))
                                                                ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer) else
                                                                ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                                            modifier = Modifier
                                                                .size(24.dp)
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
                                                    color = if (isCurrentlyChosen(L2LootScreens.Settings.ordinal))
                                                        MaterialTheme.colorScheme.secondary else
                                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            },
                                            modifier = Modifier
                                                .pointerHoverIcon(PointerIcon.Hand)
                                        )
                                    }
                                    
                                    Text(
                                        text = "v${BuildConfig.VERSION_NAME}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(bottom = LocalSpacing.current.space16)
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