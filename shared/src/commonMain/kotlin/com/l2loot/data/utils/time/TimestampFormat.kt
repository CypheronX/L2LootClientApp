package com.l2loot.data.utils.time

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diffMs = now.time - date.time
    val diffMinutes = diffMs / 60000
    val diffHours = diffMinutes / 60
    val diffDays = diffHours / 24

    return when {
        diffMinutes < 1 -> "Just now"
        diffMinutes < 60 -> "$diffMinutes min ago"
        diffHours < 24 -> "$diffHours hours ago"
        diffDays < 7 -> "$diffDays days ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}