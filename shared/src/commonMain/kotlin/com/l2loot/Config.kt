package com.l2loot

/**
 * Application configuration compiled at build time
 * URLs and flavor-specific settings are injected during build from environment variables or local.properties
 * Version is read from version.properties file
 *
 * For local development: Set values in local.properties file
 * For CI/CD: Set environment variables in the build environment
 */
object Config {
    val VERSION_NAME: String = BuildKonfig.VERSION_NAME
    val BUILD_FLAVOR: String = BuildKonfig.BUILD_FLAVOR
    val APP_NAME: String = BuildKonfig.APP_NAME
    val DB_DIR_NAME: String = BuildKonfig.DB_DIR_NAME
    val IS_DEBUG: Boolean = BuildKonfig.IS_DEBUG
    val ANALYTICS_URL: String = BuildKonfig.ANALYTICS_URL
    val SELLABLE_ITEMS_URL: String = BuildKonfig.SELLABLE_ITEMS_URL
    val ANONYMOUS_AUTH_URL: String = BuildKonfig.ANONYMOUS_AUTH_URL
    val EXTERNAL_LINKS_URL: String = BuildKonfig.EXTERNAL_LINKS_URL
}
