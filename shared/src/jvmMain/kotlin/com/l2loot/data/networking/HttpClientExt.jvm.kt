package com.l2loot.data.networking

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
    handleResponse: (HttpResponse) -> Result<T, DataError.Remote>
): Result<T, DataError.Remote> {
    return try {
        val response = execute()
        handleResponse(response)
    } catch(e: UnknownHostException) {
        Result.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: UnresolvedAddressException) {
        Result.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: ConnectException) {
        Result.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: SocketTimeoutException) {
        Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch(e: HttpRequestTimeoutException) {
        Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch(e: SerializationException) {
        Result.Failure(DataError.Remote.SERIALIZATION)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        Result.Failure(DataError.Remote.UNKNOWN)
    }
}