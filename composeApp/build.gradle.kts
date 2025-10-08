import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinx.serialization)
}

val versionPropertiesInputStream = FileInputStream("$rootDir/version.properties")
val versionProperties = Properties().apply {
    load(versionPropertiesInputStream)
}
val versionMajorProperty = versionProperties.getProperty("versionMajor").toInt()
val versionMinorProperty = versionProperties.getProperty("versionMinor").toInt()
val versionPatchProperty = versionProperties.getProperty("versionPatch").toInt()

val versionNameProperty = "$versionMajorProperty.$versionMinorProperty.$versionPatchProperty"

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sql.delight)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.l2loot.MainKt"
        
        jvmArgs += listOf("--enable-native-access=ALL-UNNAMED")

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "L2Loot"
            packageVersion = versionNameProperty
            description = "Lineage 2 QoL app for Spoilers"
            copyright = "© 2025 L2Loot. All rights reserved."
            vendor = "L2Loot"
            
            modules(
                "java.sql",
                "java.net.http",
                "jdk.unsupported",
                "java.management",
                "java.naming",
                "java.instrument"
            )
            
            windows {
                iconFile.set(project.file("src/jvmMain/composeResources/files/app_icon/spoil_logo.ico"))
                menuGroup = "L2Loot"
                upgradeUuid = "a8e9c7c4-5f4d-4e8a-9c3b-8f2d1e4a5b6c"
                perUserInstall = true
                dirChooser = true
                shortcut = true
                menu = true
            }
        }

        buildTypes.release {
            proguard {
                obfuscate.set(true)
                configurationFiles.from("proguard-rules.pro")
            }
        }
    }
}
