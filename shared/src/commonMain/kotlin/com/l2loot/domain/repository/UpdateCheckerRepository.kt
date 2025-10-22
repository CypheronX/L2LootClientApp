package com.l2loot.domain.repository

import com.l2loot.domain.model.UpdateInfo

interface UpdateCheckerRepository {
    suspend fun checkForUpdate(currentVersion: String): UpdateInfo?
}