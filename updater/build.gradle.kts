import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()
    
    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            
            implementation(project(":shared"))
            
            implementation(libs.kotlinx.serialization.json)
            
            // For HTTP downloads
            implementation(libs.bundles.ktor.common)
            implementation(libs.ktor.client.okhttp)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.l2loot.updater.MainKt"
        
        nativeDistributions {
            packageName = "L2Loot Updater"
            description = "L2Loot Application Updater"
            vendor = "Alexandr Balev"
            
            modules(
                "java.net.http",
                "jdk.unsupported"
            )
            
            windows {
                iconFile.set(project.file("src/jvmMain/resources/updater_icon.ico"))
                menuGroup = "L2Loot"
                upgradeUuid = "f1e2d3c4-5b6a-7c8d-9e0f-1a2b3c4d5e6f"
                perUserInstall = false
                dirChooser = false
                shortcut = false
                menu = false
            }
        }
        
        buildTypes.release {
            proguard {
                isEnabled.set(false)
            }
        }
    }
}

