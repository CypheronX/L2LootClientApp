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
    System.getenv(key)?.let { return it }

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
    
    // Determine flavor from project property
    val flavor = project.findProperty("buildkonfig.flavor") as? String ?: "prod"
    val isProd = flavor == "prod"

    // Use single defaultConfigs that checks the flavor
    defaultConfigs {
        buildConfigField(STRING, "VERSION_NAME", versionName)
        buildConfigField(STRING, "BUILD_FLAVOR", if (isProd) "prod" else "dev")
        buildConfigField(STRING, "APP_NAME", if (isProd) "L2Loot" else "L2Loot Dev")
        buildConfigField(STRING, "DB_DIR_NAME", if (isProd) "L2Loot" else "L2LootDev")
        buildConfigField(BOOLEAN, "IS_DEBUG", if (isProd) "false" else "true")
        buildConfigField(STRING, "GITHUB_RELEASE_REPO", if (isProd) "aleksbalev/L2LootClientAppReleases" else "aleksbalev/L2LootClientAppTest")
        buildConfigField(STRING, "GITHUB_TOKEN", if (isProd) "" else getConfigValue("GITHUB_TOKEN", ""))
        buildConfigField(STRING, "ANALYTICS_URL", if (isProd) getConfigValue("FIREBASE_ANALYTICS_URL") else getConfigValue("FIREBASE_ANALYTICS_URL_DEV", getConfigValue("FIREBASE_ANALYTICS_URL")))
        buildConfigField(STRING, "SELLABLE_ITEMS_URL", if (isProd) getConfigValue("SELLABLE_ITEMS_URL") else getConfigValue("SELLABLE_ITEMS_URL_DEV", getConfigValue("SELLABLE_ITEMS_URL")))
        buildConfigField(STRING, "ANONYMOUS_AUTH_URL", if (isProd) getConfigValue("ANONYMOUS_AUTH_URL") else getConfigValue("ANONYMOUS_AUTH_URL_DEV", getConfigValue("ANONYMOUS_AUTH_URL")))
        buildConfigField(STRING, "EXTERNAL_LINKS_URL", if (isProd) getConfigValue("EXTERNAL_LINKS_URL") else getConfigValue("EXTERNAL_LINKS_URL_DEV", getConfigValue("EXTERNAL_LINKS_URL")))
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

