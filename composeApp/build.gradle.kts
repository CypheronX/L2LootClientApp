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
            targetFormats(TargetFormat.Msi)
            
            val flavor = project.findProperty("buildkonfig.flavor") as? String ?: "prod"
            
            packageName = when (flavor) {
                "prod" -> "L2Loot"
                "stage" -> "L2Loot Stage"
                else -> "L2Loot Dev"
            }
            packageVersion = versionNameProperty
            description = when (flavor) {
                "prod" -> "Lineage 2 QoL app for Spoilers"
                "stage" -> "Lineage 2 QoL app for Spoilers (Stage)"
                else -> "Lineage 2 QoL app for Spoilers (Dev)"
            }
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
                menuGroup = when (flavor) {
                    "prod" -> "L2Loot"
                    "stage" -> "L2Loot Stage"
                    else -> "L2Loot Dev"
                }
                upgradeUuid = when (flavor) {
                    "prod" -> "a8e9c7c4-5f4d-4e8a-9c3b-8f2d1e4a5b6c"
                    "stage" -> "c0e1f9f6-7f6f-6f0c-be5d-0f4f3f6c7d8e"
                    else -> "b9f0d8d5-6f5e-5f9b-ad4c-9f3e2f5b6c7d"
                }
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

tasks.register("packageMsiProd") {
    group = "distribution"
    description = "Package MSI installer for Production"
    
    doFirst {
        println("Building Production MSI...")
        println("Note: Make sure to run: ./gradlew clean before switching flavors")
        
        val flavor = project.findProperty("buildkonfig.flavor") as? String ?: "prod"
        if (flavor != "prod") {
            println("WARNING: buildkonfig.flavor is '$flavor' but building production MSI!")
            println("Run with: ./gradlew packageReleaseMsi -Pbuildkonfig.flavor=prod")
        }
    }
}

tasks.register("packageMsiDev") {
    group = "distribution"
    description = "Package MSI installer for Development"
    
    doFirst {
        println("Building Development MSI...")
        println("Note: Make sure to run: ./gradlew clean before switching flavors")
        
        val flavor = project.findProperty("buildkonfig.flavor") as? String ?: "prod"
        if (flavor != "dev") {
            println("WARNING: buildkonfig.flavor is '$flavor' but building development MSI!")
            println("Run with: ./gradlew packageReleaseMsi -Pbuildkonfig.flavor=dev")
        }
    }
}

tasks.register("packageMsiStage") {
    group = "distribution"
    description = "Package MSI installer for Stage/Testing"
    
    doFirst {
        println("Building Stage MSI...")
        println("Note: Make sure to run: ./gradlew clean before switching flavors")
        
        val flavor = project.findProperty("buildkonfig.flavor") as? String ?: "prod"
        if (flavor != "stage") {
            println("WARNING: buildkonfig.flavor is '$flavor' but building stage MSI!")
            println("Run with: ./gradlew packageReleaseMsi -Pbuildkonfig.flavor=stage")
        }
    }
}

tasks.register<Zip>("zipAppUpdate") {
    group = "distribution"
    description = "Create update ZIP from built app distributable"
    
    val flavor = project.findProperty("buildkonfig.flavor") as? String ?: "prod"
    val appName = when (flavor) {
        "prod" -> "L2Loot"
        "stage" -> "L2Loot Stage"
        else -> "L2Loot Dev"
    }
    
    // Depend on both MSI creation and app folder generation
    dependsOn("packageReleaseMsi", "createReleaseDistributable")
    
    from(layout.buildDirectory.dir("compose/binaries/main-release/app/$appName"))
    
    destinationDirectory.set(layout.buildDirectory.dir("compose/binaries/main-release/update"))
    archiveFileName.set(when (flavor) {
        "prod" -> "L2Loot-Update-$versionNameProperty.zip"
        "stage" -> "L2Loot-Stage-Update-$versionNameProperty.zip"
        else -> "L2Loot-Dev-Update-$versionNameProperty.zip"
    })
}

tasks.register("createAppImage") {
    group = "distribution"
    description = "Create runtime image for the app"
    
    dependsOn("createRuntimeImage", "proguardReleaseJars")
    
    doLast {
        println("App image created successfully")
    }
}

tasks.register<Copy>("copyUpdaterToResources") {
    group = "distribution"
    description = "Copy updater JAR to app resources"
    
    dependsOn(":updater:packageReleaseUberJarForCurrentOS")
    
    from("${rootProject.projectDir}/updater/build/compose/jars")
    
    into("src/jvmMain/composeResources/files/updater")

    include("*.jar")
    
    rename { "L2LootUpdater.jar" }
}

// Ensure resource copying tasks wait for updater to be copied
tasks.matching { it.name == "copyNonXmlValueResourcesForJvmMain" }.configureEach {
    dependsOn("copyUpdaterToResources")
}
