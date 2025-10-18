package com.l2loot.data.firebase

import com.l2loot.Config
import com.l2loot.data.networking.post
import com.l2loot.domain.firebase.AnalyticsService
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.util.Result
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.*

class AnalyticsServiceImpl(
    private val httpClient: HttpClient,
    private val logger: LootLogger
) : AnalyticsService {
    
    private var userGuid: String = ""
    private var trackingEnabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.IO)
    
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
            val eventName = if (isFirstOpen) "first_open" else "app_open"
            sendEvent(
                eventName = eventName,
                parameters = mapOf(
                    "engagement_time_msec" to 100,
                    "session_id" to System.currentTimeMillis().toString()
                )
            )
        }
    }
    
    override fun trackSupportLinkClick(platform: String, source: String) {
        if (!trackingEnabled) {
            return
        }
        
        scope.launch {
            sendEvent(
                eventName = "support_link_click",
                parameters = mapOf(
                    "platform" to platform,
                    "source" to source
                )
            )
        }
    }
    
    private suspend fun sendEvent(
        eventName: String,
        parameters: Map<String, Any> = emptyMap()
    ) {
        if (userGuid.isEmpty()) {
            logger.debug("Analytics: User GUID not set, skipping event")
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
        
        val payload = ProxyPayload(
            eventName = eventName,
            parameters = jsonParams,
            clientId = userGuid,
            userId = userGuid,
            timestamp = System.currentTimeMillis() / 1000
        )
        
        val result = httpClient.post<ProxyPayload, Unit>(
            route = Config.ANALYTICS_URL,
            body = payload
        )
        
        when (result) {
            is Result.Success -> {
                logger.debug("Analytics event '$eventName' sent successfully")
            }
            is Result.Failure -> {
                logger.debug("Failed to send analytics event '$eventName': ${result.error}")
            }
        }
    }
}

@Serializable
private data class ProxyPayload(
    val eventName: String,
    val parameters: JsonElement,
    val clientId: String,
    val userId: String,
    val timestamp: Long
)

/**
 * Generate a unique GUID for the user
 */
fun generateUserGuid(): String {
    return UUID.randomUUID().toString()
}
