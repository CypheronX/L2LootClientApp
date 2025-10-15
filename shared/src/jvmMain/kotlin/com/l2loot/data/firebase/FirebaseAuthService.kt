package com.l2loot.data.firebase

import com.l2loot.BuildConfig
import com.l2loot.Config
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Properties

class FirebaseAuthServiceImpl : FirebaseAuthService {

    private var currentIdToken: String? = null
    private var tokenExpirationTime: Long = 0
    private val mutex = Mutex()
    
    private val tokenCacheFile: File by lazy {
        val appDataDir = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "L2Loot")
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }
        File(appDataDir, "auth_cache.properties")
    }
    
    init {
        loadPersistedToken()
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
                isLenient = true
            })
        }
    }

    override suspend fun getIdToken(): String? {
        mutex.withLock {
            if (currentIdToken != null && System.currentTimeMillis() < tokenExpirationTime - 300000) {
                if (BuildConfig.DEBUG) {
                    val remainingMinutes = (tokenExpirationTime - System.currentTimeMillis()) / 60000
                    println("ℹ️ Reusing cached auth token (${remainingMinutes} min remaining)")
                }
                return currentIdToken
            }

            if (signInAnonymously()) {
                return currentIdToken
            }

            return null
        }
    }

    override suspend fun signInAnonymously(): Boolean {
        return try {
            val response = client.post(Config.ANONYMOUS_AUTH_URL) {
                contentType(ContentType.Application.Json)
                setBody(EmptyRequest()) // Empty body for anonymous auth
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<SignInResponse>()
                currentIdToken = authResponse.idToken

                val expiresInMs = authResponse.expiresIn.toLongOrNull()?.times(1000) ?: 3600000
                tokenExpirationTime = System.currentTimeMillis() + expiresInMs

                persistToken()

                println("✅ Firebase anonymous auth successful (cached for ${expiresInMs/1000/60} minutes)")
                true
            } else {
                println("❌ Firebase auth failed: ${response.status}")
                false
            }
        } catch (e: Exception) {
            println("❌ Firebase auth error: ${e.message}")
            e.printStackTrace()
            false
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
                    
                    if (BuildConfig.DEBUG) {
                        val remainingMinutes = (expiration - System.currentTimeMillis()) / 60000
                        println("✅ Loaded cached auth token (${remainingMinutes} min remaining)")
                    }
                } else {
                    tokenCacheFile.delete()
                    if (BuildConfig.DEBUG) {
                        println("ℹ️ Cached token expired, will request new one")
                    }
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                println("⚠️ Failed to load cached token: ${e.message}")
            }
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
            
            if (BuildConfig.DEBUG) {
                println("💾 Auth token cached to: ${tokenCacheFile.absolutePath}")
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                println("⚠️ Failed to cache token: ${e.message}")
            }
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

