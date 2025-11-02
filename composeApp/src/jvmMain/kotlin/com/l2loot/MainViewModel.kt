package com.l2loot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2loot.data.firebase.generateUserGuid
import com.l2loot.domain.firebase.AnalyticsService
import com.l2loot.domain.firebase.FirebaseAuthService
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.model.UserSettings
import com.l2loot.domain.repository.LoadDbDataRepository
import com.l2loot.domain.repository.SellableRepository
import com.l2loot.domain.repository.UserSettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthState {
    Loading, Success, Failed
}

class MainViewModel(
    private val loadDbDataRepository: LoadDbDataRepository,
    private val sellableRepository: SellableRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val analyticsService: AnalyticsService,
    private val firebaseAuthService: FirebaseAuthService,
    private val logger: LootLogger
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val isDatabaseEmpty = loadDbDataRepository.isDatabaseEmpty()
    val dbLoadProgress = loadDbDataRepository.progress
    
    val state: StateFlow<MainState> = combine(_state, dbLoadProgress) { state, progress ->
        val isLoading = !state.isStartupComplete || (isDatabaseEmpty && progress < 1.0f)
        state.copy(isLoading = isLoading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainState()
    )

    init {
        initializeApp()
    }

    private fun initializeApp() {
        // Initialize user settings defaults
        viewModelScope.launch {
            userSettingsRepository.initializeDefaults()
        }

        // Handle startup progress
        viewModelScope.launch {
            if (!isDatabaseEmpty) {
                for (i in 0..100 step 5) {
                    _state.update {
                        it.copy(startupProgress = i / 100f)
                    }
                    delay(10)
                }
                _state.update {
                    it.copy(
                        startupProgress = 1f,
                        isStartupComplete = true
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isStartupComplete = true
                    )
                }
                loadDbDataRepository.load()
            }
        }

        // Handle authentication
        viewModelScope.launch {
            val token = firebaseAuthService.getIdToken()
            val newAuthState = if (token != null) AuthState.Success else AuthState.Failed
            _state.update {
                it.copy(
                    authState = newAuthState
                )
            }
            
            if (token == null) {
                logger.warn("⚠️ Firebase authentication failed - some features may be unavailable")
            }
        }

        // Handle user settings and analytics
        viewModelScope.launch {
            val settings = userSettingsRepository.getSettings().firstOrNull()
            val isFirstOpen = settings?.userGuid.isNullOrEmpty()
            
            if (isFirstOpen) {
                val newGuid = generateUserGuid()
                analyticsService.setUserGuid(newGuid)
                analyticsService.setTrackingEnabled(true)
                
                userSettingsRepository.updateUserGuid(newGuid)
                userSettingsRepository.updateTrackEvents(true)
                
                analyticsService.trackAppOpen(isFirstOpen = true)
                
                _state.update { it.copy(shouldShowConsentAfterLoad = true) }
            } else {
                analyticsService.setUserGuid(settings.userGuid)
                analyticsService.setTrackingEnabled(settings.trackEvents)
                analyticsService.trackAppOpen(isFirstOpen = false)
            }
            
            userSettingsRepository.incrementAppOpenCount()
            userSettingsRepository.incrementSessionCountSincePrompt()
            
            val (shouldShowSupport, isReminder) = shouldShowSupportDialog(settings)
            
            if (shouldShowSupport && !isFirstOpen) {
                // Wait for loading and consent dialog to finish
                while (state.value.isLoading || state.value.showConsentDialog) {
                    delay(100)
                }
                delay(500)
                _state.update {
                    it.copy(
                        showSupportDialog = true,
                        isSupportDialogReminder = isReminder
                    )
                }
                userSettingsRepository.updateLastPromptDate(System.currentTimeMillis())
            }
        }

        // Handle Managed prices fetch
        viewModelScope.launch {
            state.collect { currentState ->
                val authState = currentState.authState
                if (authState == AuthState.Success) {
                    try {
                        val settings = userSettingsRepository.getSettings().firstOrNull()
                        if (settings?.isManagedPrices == true) {
                            sellableRepository.fetchManagedPrices(
                                serverName = settings.serverName,
                                forceRefresh = true
                            )
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to auto-fetch Aynix prices on startup", e)
                    }
                } else if (authState == AuthState.Failed) {
                    logger.warn("Skipping Aynix price fetch due to authentication failure")
                }
            }
        }
    }

    fun showConsentDialog() {
        _state.update { it.copy(showConsentDialog = true) }
    }

    fun hideConsentDialog() {
        _state.update { it.copy(showConsentDialog = false) }
    }

    fun hideSupportDialog() {
        _state.update { it.copy(showSupportDialog = false) }
    }

    fun hideUpdateNotification() {
        _state.update { it.copy(showUpdateNotification = false) }
    }

    fun acceptConsent() {
        viewModelScope.launch {
            val currentGuid = analyticsService.getUserGuid()
            userSettingsRepository.updateUserGuid(currentGuid)
            userSettingsRepository.updateTrackEvents(trackEvents = true)
            analyticsService.setTrackingEnabled(true)
        }
        hideConsentDialog()
    }

    fun declineConsent() {
        viewModelScope.launch {
            val currentGuid = analyticsService.getUserGuid()
            userSettingsRepository.updateUserGuid(currentGuid)
            userSettingsRepository.updateTrackEvents(trackEvents = false)
            analyticsService.setTrackingEnabled(false)
        }
        hideConsentDialog()
    }

    fun updateSupportClickDate(timestamp: Long) {
        viewModelScope.launch {
            userSettingsRepository.updateLastSupportClickDate(timestamp)
        }
    }

    private fun shouldShowSupportDialog(settings: UserSettings?): Pair<Boolean, Boolean> {
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
}