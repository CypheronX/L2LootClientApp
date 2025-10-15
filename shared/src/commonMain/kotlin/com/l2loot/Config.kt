package com.l2loot

/**
 * Application configuration loaded from environment variables.
 * URLs are not stored in source code for security.
 * 
 * Set these environment variables before running:
 * - FIREBASE_ANALYTICS_URL
 * - FIREBASE_SELLABLE_ITEMS_URL
 * - FIREBASE_ANONYMOUS_AUTH_URL
 * - FIREBASE_PROJECT_ID
 * 
 * Or configure them in local.properties file.
 */
object Config {
    val ANALYTICS_URL: String by lazy {
        getEnvOrDefault("FIREBASE_ANALYTICS_URL", "https://example.com/analytics")
    }
    
    val SELLABLE_ITEMS_URL: String by lazy {
        getEnvOrDefault("FIREBASE_SELLABLE_ITEMS_URL", "https://example.com/sellableitems")
    }
    
    val ANONYMOUS_AUTH_URL: String by lazy {
        getEnvOrDefault("FIREBASE_ANONYMOUS_AUTH_URL", "https://example.com/anonymousauth")
    }
    
    val FIREBASE_PROJECT_ID: String by lazy {
        getEnvOrDefault("FIREBASE_PROJECT_ID", "your-project-id")
    }
}

expect fun getEnvOrDefault(key: String, defaultValue: String): String
