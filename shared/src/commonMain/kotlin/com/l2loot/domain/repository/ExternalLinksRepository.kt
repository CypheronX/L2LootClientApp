package com.l2loot.domain.repository

import com.l2loot.domain.model.ExternalLinks
import com.l2loot.domain.util.DataError
import com.l2loot.domain.util.Result

interface ExternalLinksRepository {

    suspend fun fetchExternalLinks(): Result<ExternalLinks, DataError.Remote>
}