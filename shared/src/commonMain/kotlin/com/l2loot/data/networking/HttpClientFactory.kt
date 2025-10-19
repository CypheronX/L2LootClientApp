package com.l2loot.data.networking

import io.ktor.client.plugins.logging.Logger
import com.l2loot.Config
import com.l2loot.domain.logging.LootLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val lootLogger: LootLogger
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

            }
        }
    }
}