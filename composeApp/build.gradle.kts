import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinx.serialization)
}

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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "L2Loot"
            packageVersion = "1.0.0"
            description = "Lineage 2 Spoil and Loot Calculator"
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
            
            macOS {
                iconFile.set(project.file("src/jvmMain/composeResources/files/app_icon/spoil_logo.png"))
                bundleID = "com.l2loot"
            }
            
            linux {
                iconFile.set(project.file("src/jvmMain/composeResources/files/app_icon/spoil_logo.png"))
            }
        }
        
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}
