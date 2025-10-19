package com.l2loot.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExternalLinksRaw(
    val market_owners_discord: String
)

data class ExternalLinks(
    val marketOwnersDiscord: String
)

fun ExternalLinksRaw.toDomainModel(): ExternalLinks {
    return ExternalLinks(
        marketOwnersDiscord = market_owners_discord
    )
}
