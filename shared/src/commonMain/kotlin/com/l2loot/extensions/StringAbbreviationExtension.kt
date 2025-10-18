package com.l2loot.extensions

fun String.abbreviationMatch(query: String): Boolean {
    val normalized = this.replace("_", " ")
        .split(" ")
        .map { it.firstOrNull()?.lowercaseChar() ?: "" }
        .joinToString("")

    return normalized.contains(query.lowercase(), ignoreCase = true)
}