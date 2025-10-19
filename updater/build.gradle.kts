import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
            
            // For HTTP downloads
            implementation(libs.bundles.ktor.common)
            implementation(libs.ktor.client.okhttp)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.l2loot.updater.MainKt"
        
        // Don't need native distributions - we'll use UberJar instead
        // UberJar is much smaller and can use the main app's bundled JVM
        
        buildTypes.release {
            proguard {
                isEnabled.set(false)
            }
        }
    }
}

