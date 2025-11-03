package com.l2loot.data.firebase

import com.l2loot.Config
import com.l2loot.data.networking.models.EmptyRequest
import com.l2loot.data.networking.models.SignInResponse
import com.l2loot.data.networking.post
import com.l2loot.domain.firebase.FirebaseAuthService
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.util.Result
import io.ktor.client.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.Properties

class FirebaseAuthServiceImpl(
    private val httpClient: HttpClient,
    private val logger: LootLogger
) : FirebaseAuthService {

    private var currentIdToken: String? = null
    private var tokenExpirationTime: Long = 0
    private val mutex = Mutex()
    
    companion object {
        private const val DEFAULT_TOKEN_LIFETIME_MS = 3600000L // 1 hour (fallback)
        private const val TOKEN_REFRESH_BUFFER_MS = 300000L // 5 minutes
    }
    
    private val tokenCacheFile: File by lazy {
        val appDataDir = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), Config.DB_DIR_NAME)
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }
        File(appDataDir, "auth_cache.properties")
    }
    
    private val versionFile: File by lazy {
        val appDataDir = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), Config.DB_DIR_NAME)
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }
        File(appDataDir, "app_version.properties")
    }
    
    init {
        checkVersionAndClearCacheIfNeeded()
        loadPersistedToken()
    }
    
    /**
     * Check if app version has changed and clear auth cache if so
     * This ensures stale tokens from previous versions don't cause issues
     * 
     * Clears cache if:
     * - Version file doesn't exist (upgrading from old version without tracking)
     * - Version file exists but version is different
     */
    private fun checkVersionAndClearCacheIfNeeded() {
        try {
            val currentVersion = Config.VERSION_NAME
            var lastVersion: String? = null
            var shouldClearCache = false
            
            if (versionFile.exists()) {
                val props = Properties()
                versionFile.inputStream().use { props.load(it) }
                lastVersion = props.getProperty("version")
                
                if (lastVersion != currentVersion) {
                    logger.info("App version changed from $lastVersion to $currentVersion - clearing auth cache")
                    shouldClearCache = true
                }
            } else {
                logger.info("No version tracking file found - likely first run after upgrade - clearing auth cache")
                shouldClearCache = true
            }
            
            if (shouldClearCache && tokenCacheFile.exists()) {
                tokenCacheFile.delete()
                logger.info("Auth cache cleared successfully")
            }
            
            val props = Properties()
            props.setProperty("version", currentVersion)
            props.setProperty("lastUpdated", System.currentTimeMillis().toString())
            versionFile.outputStream().use { props.store(it, "App Version Tracking") }
            
        } catch (e: Exception) {
            logger.warn("Failed to check version: ${e.message}")
        }
    }

    override suspend fun getIdToken(): String? {
        mutex.withLock {
            if (currentIdToken != null && System.currentTimeMillis() < tokenExpirationTime - TOKEN_REFRESH_BUFFER_MS) {
                val remainingMinutes = (tokenExpirationTime - System.currentTimeMillis()) / 60000
                logger.debug("Reusing cached auth token ($remainingMinutes min remaining)")
                return currentIdToken
            }

            if (signInAnonymously()) {
                return currentIdToken
            }

            return null
        }
    }

    override suspend fun signInAnonymously(): Boolean {
        val result = httpClient.post<EmptyRequest, SignInResponse>(
            route = Config.ANONYMOUS_AUTH_URL,
            body = EmptyRequest()
        )
        
        return when (result) {
            is Result.Success -> {
                val authResponse = result.data
                currentIdToken = authResponse.idToken

                val expiresInMs = authResponse.expiresIn.toLongOrNull()?.times(1000) ?: DEFAULT_TOKEN_LIFETIME_MS
                tokenExpirationTime = System.currentTimeMillis() + expiresInMs

                persistToken()
                true
            }
            is Result.Failure -> {
                logger.error("Firebase auth failed: ${result.error}")
                false
            }
        }
    }
    
    /**
     * Load persisted token from cache file
     */
    private fun loadPersistedToken() {
        try {
            if (tokenCacheFile.exists()) {
                val props = Properties()
                tokenCacheFile.inputStream().use { props.load(it) }
                
                val token = props.getProperty("idToken")
                val expiration = props.getProperty("expiresAt")?.toLongOrNull()
                
                if (token != null && expiration != null && System.currentTimeMillis() < expiration) {
                    currentIdToken = token
                    tokenExpirationTime = expiration
                } else {
                    tokenCacheFile.delete()
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to load cached token: ${e.message}")
        }
    }
    
    /**
     * Persist token to cache file
     */
    private fun persistToken() {
        try {
            val props = Properties()
            props.setProperty("idToken", currentIdToken)
            props.setProperty("expiresAt", tokenExpirationTime.toString())
            
            tokenCacheFile.outputStream().use { props.store(it, "Firebase Auth Token Cache") }
        } catch (e: Exception) {
            logger.warn("Failed to cache token: ${e.message}")
        }
    }
}
