import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sql.delight)
}

sqldelight {
    databases {
        create("L2LootDatabase") {
            packageName.set("com.l2loot")
            deriveSchemaFromMigrations.set(false)
            // Disable verification - we manage schema in .sq files
            // Migrations are only for upgrading existing user databases
            verifyMigrations.set(false)
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.0.2")
        }
    }
}

fun getConfigValue(key: String): String {
    System.getenv(key)?.let { return it }

    val localProperties = rootProject.file("local.properties")
    if (localProperties.exists()) {
        val localProperties = Properties()
        localProperties.load(localPropertiesFile.inputStream())
        localProperties.getProperty(key)?.let { return it }
    }

    return ""
}

val firebaseAnalyticsUrl = getConfigValue("FIREBASE_ANALYTICS_URL")
val sellableItemsUrl = getConfigValue("SELLABLE_ITEMS_URL")
val anonymousAuthUrl = getConfigValue("ANONYMOUS_AUTH_URL")

val generateConfigTask = tasks.register("generateAppConfig") {
    val outputDir = layout.buildDirectory.dir("generated/source/appconfig/commonMain/kotlin").get().asFile.path
    val outputFile = file("$outputDir/com/l2loot/AppConfig.kt")

    outputs.dir(outputDir)
    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()
        outputFile.writeText("""
            package com.l2loot
            
            /**
              * Application configuration generated at build time.
              * There values are compiled into the application.
              */
            object AppConfig {
                const val ANALYTICS_URL = "$firebaseAnalyticsUrl"
                const val SELLABLE_ITEMS_URL = "$sellableItemsUrl"
                const val ANONYMOUS_AUTH_URL = "$anonymousAuthUrl"
            }
        """.trimIndent())
    }
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain {
            kotlin.srcDirs(layout.buildDirectory.dir("generated/source/appconfig/commonMain/kotlin").get().asFile.path)
            dependencies {
                api(libs.koin.core)
                implementation(libs.touchlab.kermit)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.sql.delight)
                implementation(libs.sql.delight.coroutines)

                implementation(libs.bundles.ktor.common)
                implementation(libs.touchlab.kermit)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
//            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.okhttp)
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateConfigTask)
}
