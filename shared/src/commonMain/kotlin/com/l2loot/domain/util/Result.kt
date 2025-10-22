package com.l2loot.domain.util

sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Failure<out E: Error>(val error: E): Result<Nothing, E>
}

inline fun <T, E: Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Success -> Result.Success(map(data))
        is Result.Failure -> Result.Failure(error)
    }
}

inline fun <T, E: Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Success -> {
            action(data)
            this
        }
        is Result.Failure -> this
    }
}

inline fun <T, E: Error> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Success -> this
        is Result.Failure -> {
            action(error)
            this
        }
    }
}

fun <T, E: Error> Result<T, E>.asEmptyResult(): EmptyResult<E> {
    return map { }
}

typealias EmptyResult<E> = Result<Unit, E>
