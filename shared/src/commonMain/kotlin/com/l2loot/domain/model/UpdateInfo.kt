package com.l2loot.domain.model

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val releaseUrl: String,
    val releaseNotes: String
)