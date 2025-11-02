package com.l2loot.data.firebase

import com.l2loot.Config
import com.l2loot.data.networking.post
import com.l2loot.domain.firebase.FirebaseAuthService
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.util.Result
import io.ktor.client.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
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
        private const val DEFAULT_TOKEN_LIFETIME_MS = 86400000L // 24 hours
        private const val TOKEN_REFRESH_BUFFER_MS = 300000L // 5 minutes
    }
    
    private val tokenCacheFile: File by lazy {
        val appDataDir = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), Config.DB_DIR_NAME)
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }
        File(appDataDir, "auth_cache.properties")
    }
    
    init {
        loadPersistedToken()
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

@Serializable
private class EmptyRequest

@Serializable
private data class SignInResponse(
    val idToken: String,
    val expiresIn: String,
    val refreshToken: String? = null,
    val localId: String? = null
)

