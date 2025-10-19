package com.l2loot

/**
 * Application configuration compiled at build time
 * URLs are injected during build from environment variables or local.properties
 *
 * For local development: Set values in local.properties file
 * For CI/CD: Set environment variables in the build environment
 */
object Config {
    const val ANALYTICS_URL: String = AppConfig.ANALYTICS_URL
    const val SELLABLE_ITEMS_URL: String = AppConfig.SELLABLE_ITEMS_URL
    const val ANONYMOUS_AUTH_URL: String = AppConfig.ANONYMOUS_AUTH_URL
    const val EXTERNAL_LINKS_URL: String = AppConfig.EXTERNAL_LINKS_URL
}
