
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.sql.delight)
}

sqldelight {
    databases {
        create("L2LootDatabase") {
            packageName.set("com.l2loot.db")
        }
    }
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.sql.delight)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

