package com.l2loot.data.analytics

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.*

/**
 * Analytics service for tracking user events using Firebase Google Analytics 4 Measurement Protocol.
 * 
 * Note: You need to replace MEASUREMENT_ID and API_SECRET with your actual Firebase values.
 * Get these from: Firebase Console > Your Project > Settings > Data Streams > Web/App > Measurement Protocol API secrets
 */
interface AnalyticsService {
    /**
     * Track app opening event
     * @param isFirstOpen Whether this is the first time the app is opened
     */
    fun trackAppOpen(isFirstOpen: Boolean)
    
    /**
     * Track support link clicks
     * @param platform The support platform (patreon or kofi)
     * @param source Where the link was clicked (dialog or settings)
     */
    fun trackSupportLinkClick(platform: String, source: String)
    
    /**
     * Set the user GUID for analytics
     */
    fun setUserGuid(guid: String)
    
    /**
     * Get the current user GUID
     */
    fun getUserGuid(): String
    
    /**
     * Enable or disable event tracking
     */
    fun setTrackingEnabled(enabled: Boolean)
}

class AnalyticsServiceImpl(
    private val measurementId: String = "G-J8FJFJFE3L",
    private val apiSecret: String = "vPn7f9oyQ0mKE01PYsUtDw"
) : AnalyticsService {
    
    private var userGuid: String = ""
    private var trackingEnabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }
    
    override fun setUserGuid(guid: String) {
        this.userGuid = guid
    }
    
    override fun getUserGuid(): String {
        return userGuid
    }
    
    override fun setTrackingEnabled(enabled: Boolean) {
        this.trackingEnabled = enabled
    }
    
    override fun trackAppOpen(isFirstOpen: Boolean) {
        if (!isFirstOpen && !trackingEnabled) {
            return
        }
        
        scope.launch {
            try {
                val eventName = if (isFirstOpen) "first_open" else "app_open"
                sendEvent(
                    eventName = eventName,
                    parameters = mapOf(
                        "engagement_time_msec" to 100,
                        "session_id" to System.currentTimeMillis().toString()
                    )
                )
            } catch (e: Exception) {
                println("Failed to track app open: ${e.message}")
            }
        }
    }
    
    override fun trackSupportLinkClick(platform: String, source: String) {
        if (!trackingEnabled) {
            return
        }
        
        scope.launch {
            try {
                sendEvent(
                    eventName = "support_link_click",
                    parameters = mapOf(
                        "platform" to platform,
                        "source" to source
                    )
                )
            } catch (e: Exception) {
                println("Failed to track support link click: ${e.message}")
            }
        }
    }
    
    private suspend fun sendEvent(
        eventName: String,
        parameters: Map<String, Any> = emptyMap()
    ) {
        if (userGuid.isEmpty()) {
            println("Analytics: User GUID not set, skipping event")
            return
        }
        
        val jsonParams = buildJsonObject {
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> put(key, value)
                    is Number -> put(key, value)
                    is Boolean -> put(key, value)
                    else -> put(key, value.toString())
                }
            }
        }
        
        val event = GA4Event(
            name = eventName,
            params = jsonParams
        )
        
        val payload = GA4Payload(
            client_id = userGuid,
            user_id = userGuid,
            events = listOf(event),
            timestamp_micros = System.currentTimeMillis() * 1000,
            debug_mode = true
        )
        
        try {
            val response = client.post("https://www.google-analytics.com/mp/collect") {
                parameter("measurement_id", measurementId)
                parameter("api_secret", apiSecret)
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            println("✓ Analytics event '$eventName' sent successfully - Status: ${response.status}")
        } catch (e: Exception) {
            println("✗ Failed to send analytics event '$eventName': ${e.message}")
            e.printStackTrace()
        }
    }
}

@Serializable
private data class GA4Payload(
    val client_id: String,
    val user_id: String,
    val events: List<GA4Event>,
    val timestamp_micros: Long,
    val debug_mode: Boolean
)

@Serializable
private data class GA4Event(
    val name: String,
    val params: JsonElement
)

/**
 * Generate a unique GUID for the user
 */
fun generateUserGuid(): String {
    return UUID.randomUUID().toString()
}
