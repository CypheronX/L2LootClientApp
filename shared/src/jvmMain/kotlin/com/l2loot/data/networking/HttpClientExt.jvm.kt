package com.l2loot.data.networking

import co.touchlab.kermit.Logger
import com.l2loot.domain.util.DataError
import com.l2loot.domain.util.Result
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.UnknownHostException
import kotlin.coroutines.coroutineContext

actual suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Result<T, DataError.Remote>
): Result<T, DataError.Remote> {
    return try {
        val response = execute()
        handleResponse(response)
    } catch(e: UnknownHostException) {
        Logger.e("HTTP", e) { "Network request failed: UnknownHostException - ${e.message}" }
        Result.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: UnresolvedAddressException) {
        Logger.e("HTTP", e) { "Network request failed: UnresolvedAddressException - ${e.message}" }
        Result.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: ConnectException) {
        Logger.e("HTTP", e) { "Network request failed: ConnectException - ${e.message}" }
        Result.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: SocketTimeoutException) {
        Logger.e("HTTP", e) { "Network request failed: SocketTimeoutException - ${e.message}" }
        Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch(e: HttpRequestTimeoutException) {
        Logger.e("HTTP", e) { "Network request failed: HttpRequestTimeoutException - ${e.message}" }
        Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch(e: SerializationException) {
        Logger.e("HTTP", e) { "Network request failed: SerializationException - ${e.message}" }
        Result.Failure(DataError.Remote.SERIALIZATION)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        Logger.e("HTTP", e) { "Network request failed with UNKNOWN exception: ${e::class.simpleName} - ${e.message}" }
        Result.Failure(DataError.Remote.UNKNOWN)
    }
}