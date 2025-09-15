package com.l2loot

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform