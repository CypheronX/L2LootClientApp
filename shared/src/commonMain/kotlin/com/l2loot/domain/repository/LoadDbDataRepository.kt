package com.l2loot.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface LoadDbDataRepository {
    val progress: StateFlow<Float>
    suspend fun load()
    fun isDatabaseEmpty(): Boolean
}