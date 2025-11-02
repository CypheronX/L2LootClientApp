package com.l2loot.domain.model


enum class ServerName(val serverKey: String, val displayName: String) {
    REBORN_TEON("reborn_teon", "Reborn Teon"),
    REBORN_FRANZ("reborn_franz", "Reborn Franz");

    companion object {
        fun fromKey(key: String): ServerName? {
            return entries.find { it.serverKey == key }
        }

        fun getAllKeys(): List<String> {
            return entries.map { it.serverKey }
        }

        val DEFAULT = REBORN_TEON
    }
}

