package com.l2loot.data.firebase

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

class FirebaseAuthServiceImpl(
    private val apiKey: String = "REDACTED"
) : FirebaseAuthService {
    
    private var currentIdToken: String? = null
    private var tokenExpirationTime: Long = 0
    private val mutex = Mutex()
    
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
            // Check if token is still valid (with 5 minute buffer)
            if (currentIdToken != null && System.currentTimeMillis() < tokenExpirationTime - 300000) {
                return currentIdToken
            }
            
            // Token expired or doesn't exist, sign in anonymously
            if (signInAnonymously()) {
                return currentIdToken
            }
            
            return null
        }
    }
    
    override suspend fun signInAnonymously(): Boolean {
        return try {
            val response = client.post("REDACTED=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(SignInRequest(returnSecureToken = true))
            }
            
            if (response.status.isSuccess()) {
                val authResponse = response.body<SignInResponse>()
                currentIdToken = authResponse.idToken
                
                // expiresIn is in seconds, convert to milliseconds
                val expiresInMs = authResponse.expiresIn.toLongOrNull()?.times(1000) ?: 3600000
                tokenExpirationTime = System.currentTimeMillis() + expiresInMs
                
                println("✅ Firebase anonymous auth successful")
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
}

@Serializable
private data class SignInRequest(
    val returnSecureToken: Boolean
)

@Serializable
private data class SignInResponse(
    val idToken: String,
    val email: String? = null,
    val refreshToken: String,
    val expiresIn: String,
    val localId: String
)

