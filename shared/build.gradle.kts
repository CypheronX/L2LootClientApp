
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
            verifyMigrations.set(false)
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.0.2")
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
            implementation(libs.sql.delight)
            implementation(libs.sql.delight.coroutines)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
    }
}
