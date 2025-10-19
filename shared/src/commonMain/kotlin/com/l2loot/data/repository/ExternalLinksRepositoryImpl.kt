package com.l2loot.data.repository

import com.l2loot.Config
import com.l2loot.data.networking.get
import com.l2loot.domain.model.ExternalLinks
import com.l2loot.domain.model.ExternalLinksRaw
import com.l2loot.domain.model.toDomainModel
import com.l2loot.domain.repository.ExternalLinksRepository
import com.l2loot.domain.util.DataError
import com.l2loot.domain.util.Result
import com.l2loot.domain.util.map
import com.l2loot.domain.util.onSuccess
import io.ktor.client.HttpClient

class ExternalLinksRepositoryImpl(
    private val httpClient: HttpClient
): ExternalLinksRepository {
    var cachedExternalLinks: ExternalLinks? = null

    override suspend fun fetchExternalLinks(): Result<ExternalLinks, DataError.Remote> {
        if (cachedExternalLinks != null) {
            return Result.Success(cachedExternalLinks as ExternalLinks)
        }

        return httpClient.get<ExternalLinksRaw>(
            route = Config.EXTERNAL_LINKS_URL
        ).map {
            it.toDomainModel()
        }.onSuccess {
            cachedExternalLinks = it
        }
    }
}