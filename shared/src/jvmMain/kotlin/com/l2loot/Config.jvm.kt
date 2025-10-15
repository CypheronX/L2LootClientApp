package com.l2loot

import java.io.File
import java.util.Properties

/**
 * JVM implementation that loads configuration from:
 * 1. System environment variables (highest priority)
 * 2. local.properties file (if exists)
 * 3. Default value (fallback)
 */
actual fun getEnvOrDefault(key: String, defaultValue: String): String {
    System.getenv(key)?.let { return it }
    
    val localProperties = loadLocalProperties()
    localProperties.getProperty(key)?.let { return it }
    
    return defaultValue
}

private fun loadLocalProperties(): Properties {
    val properties = Properties()
    
    try {
        // Look for local.properties in project root
        val possiblePaths = listOf(
            "local.properties",
            "../local.properties",
            "../../local.properties"
        )
        
        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists()) {
                file.inputStream().use { properties.load(it) }
                if (BuildConfig.DEBUG) {
                    println("✓ Loaded configuration from: ${file.absolutePath}")
                }
                break
            }
        }
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) {
            println("⚠️ Could not load local.properties: ${e.message}")
        }
    }
    
    return properties
}

