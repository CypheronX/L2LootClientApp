package com.l2loot.data.networking.models

import kotlinx.serialization.Serializable

@Serializable
internal class EmptyRequest

@Serializable
internal data class SignInResponse(
    val idToken: String,
    val expiresIn: String,
    val refreshToken: String? = null,
    val localId: String? = null
)

