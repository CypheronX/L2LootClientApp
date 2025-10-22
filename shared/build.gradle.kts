import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sql.delight)
    alias(libs.plugins.buildkonfig)
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

fun getConfigValue(key: String, defaultValue: String = ""): String {
    val localProperties = rootProject.file("local.properties")
    if (localProperties.exists()) {
        val properties = Properties()
        properties.load(localPropertiesFile.inputStream())
        properties.getProperty(key)?.let { return it }
    }

    return defaultValue
}

val versionPropertiesFile = rootProject.file("version.properties")
val versionProperties = Properties().apply {
    if (versionPropertiesFile.exists()) {
        load(versionPropertiesFile.inputStream())
    }
}
val versionName = "${versionProperties.getProperty("versionMajor", "1")}.${versionProperties.getProperty("versionMinor", "0")}.${versionProperties.getProperty("versionPatch", "0")}"

buildkonfig {
    packageName = "com.l2loot"
    
    // Default flavor (prod)
    defaultConfigs {
        buildConfigField(STRING, "VERSION_NAME", versionName)
        buildConfigField(STRING, "BUILD_FLAVOR", "prod")
        buildConfigField(STRING, "APP_NAME", "L2Loot")
        buildConfigField(STRING, "DB_DIR_NAME", "L2Loot")
        buildConfigField(BOOLEAN, "IS_DEBUG", "false")
        buildConfigField(STRING, "GITHUB_RELEASE_REPO", "aleksbalev/L2LootClientAppReleases")
        buildConfigField(STRING, "GITHUB_TOKEN", "")
        buildConfigField(STRING, "ANALYTICS_URL", getConfigValue("FIREBASE_ANALYTICS_URL"))
        buildConfigField(STRING, "SELLABLE_ITEMS_URL", getConfigValue("SELLABLE_ITEMS_URL"))
        buildConfigField(STRING, "ANONYMOUS_AUTH_URL", getConfigValue("ANONYMOUS_AUTH_URL"))
        buildConfigField(STRING, "EXTERNAL_LINKS_URL", getConfigValue("EXTERNAL_LINKS_URL"))
    }

    // Dev flavor
    defaultConfigs("dev") {
        buildConfigField(STRING, "VERSION_NAME", versionName)
        buildConfigField(STRING, "BUILD_FLAVOR", "dev")
        buildConfigField(STRING, "APP_NAME", "L2Loot Dev")
        buildConfigField(STRING, "DB_DIR_NAME", "L2LootDev")
        buildConfigField(BOOLEAN, "IS_DEBUG", "true")
        buildConfigField(STRING, "GITHUB_RELEASE_REPO", "aleksbalev/L2LootClientAppTest")
        buildConfigField(STRING, "GITHUB_TOKEN", getConfigValue("GITHUB_TOKEN", ""))
        buildConfigField(STRING, "ANALYTICS_URL", getConfigValue("FIREBASE_ANALYTICS_URL_DEV", getConfigValue("FIREBASE_ANALYTICS_URL")))
        buildConfigField(STRING, "SELLABLE_ITEMS_URL", getConfigValue("SELLABLE_ITEMS_URL_DEV", getConfigValue("SELLABLE_ITEMS_URL")))
        buildConfigField(STRING, "ANONYMOUS_AUTH_URL", getConfigValue("ANONYMOUS_AUTH_URL_DEV", getConfigValue("ANONYMOUS_AUTH_URL")))
        buildConfigField(STRING, "EXTERNAL_LINKS_URL", getConfigValue("EXTERNAL_LINKS_URL_DEV", getConfigValue("EXTERNAL_LINKS_URL")))
    }

    // Stage flavor (for testers - like prod but with updater testing)
    defaultConfigs("stage") {
        buildConfigField(STRING, "VERSION_NAME", versionName)
        buildConfigField(STRING, "BUILD_FLAVOR", "stage")
        buildConfigField(STRING, "APP_NAME", "L2Loot Stage")
        buildConfigField(STRING, "DB_DIR_NAME", "L2LootStage")
        buildConfigField(BOOLEAN, "IS_DEBUG", "true")
        buildConfigField(STRING, "GITHUB_RELEASE_REPO", "aleksbalev/L2LootClientAppTest")
        buildConfigField(STRING, "GITHUB_TOKEN", getConfigValue("GITHUB_TOKEN", ""))
        buildConfigField(STRING, "ANALYTICS_URL", getConfigValue("FIREBASE_ANALYTICS_URL_DEV", getConfigValue("FIREBASE_ANALYTICS_URL")))
        buildConfigField(STRING, "SELLABLE_ITEMS_URL", getConfigValue("SELLABLE_ITEMS_URL_DEV", getConfigValue("SELLABLE_ITEMS_URL")))
        buildConfigField(STRING, "ANONYMOUS_AUTH_URL", getConfigValue("ANONYMOUS_AUTH_URL_DEV", getConfigValue("ANONYMOUS_AUTH_URL")))
        buildConfigField(STRING, "EXTERNAL_LINKS_URL", getConfigValue("EXTERNAL_LINKS_URL_DEV", getConfigValue("EXTERNAL_LINKS_URL")))
    }
    
    // Prod flavor (explicit)
    defaultConfigs("prod") {
        buildConfigField(STRING, "VERSION_NAME", versionName)
        buildConfigField(STRING, "BUILD_FLAVOR", "prod")
        buildConfigField(STRING, "APP_NAME", "L2Loot")
        buildConfigField(STRING, "DB_DIR_NAME", "L2Loot")
        buildConfigField(BOOLEAN, "IS_DEBUG", "false")
        buildConfigField(STRING, "GITHUB_RELEASE_REPO", "aleksbalev/L2LootClientAppReleases")
        buildConfigField(STRING, "GITHUB_TOKEN", "")
        buildConfigField(STRING, "ANALYTICS_URL", getConfigValue("FIREBASE_ANALYTICS_URL"))
        buildConfigField(STRING, "SELLABLE_ITEMS_URL", getConfigValue("SELLABLE_ITEMS_URL"))
        buildConfigField(STRING, "ANONYMOUS_AUTH_URL", getConfigValue("ANONYMOUS_AUTH_URL"))
        buildConfigField(STRING, "EXTERNAL_LINKS_URL", getConfigValue("EXTERNAL_LINKS_URL"))
    }
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain {
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
            implementation(libs.ktor.client.okhttp)
        }
    }
}

