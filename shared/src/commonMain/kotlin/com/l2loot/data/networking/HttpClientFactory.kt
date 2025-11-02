package com.l2loot.data.networking

import io.ktor.client.plugins.logging.Logger
import com.l2loot.Config
import com.l2loot.domain.firebase.FirebaseAuthService
import com.l2loot.domain.logging.LootLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking

class HttpClientFactory(
    private val lootLogger: LootLogger,
    private val authService: FirebaseAuthService?
) {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 20_000L
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        lootLogger.debug(message)
                    }
                }
                level = if (Config.IS_DEBUG) LogLevel.ALL else LogLevel.NONE
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                
                val urlString = url.toString()
                if (authService != null && 
                    !urlString.contains("anonymousauth", ignoreCase = true) &&
                    !urlString.contains("analytics", ignoreCase = true)) {
                    runBlocking {
                        val token = authService.getIdToken()
                        if (token != null) {
                            headers.append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    }
                }
            }
        }
    }
}