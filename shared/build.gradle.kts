
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sql.delight)
}

sqldelight {
    databases {
        create("L2LootDatabase") {
            packageName.set("com.l2loot")
            deriveSchemaFromMigrations.set(false)
            verifyMigrations.set(true)
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
        }
    }
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.sql.delight)
        }
    }
}
