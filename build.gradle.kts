plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.sql.delight) apply false
}

allprojects {
    val buildFlavor = project.findProperty("buildkonfig.flavor") as? String ?: "prod"
    
    ext.set("buildkonfig.flavor", buildFlavor)
    
    gradle.taskGraph.whenReady {
        if (project.hasProperty("buildkonfig.flavor")) {
            println("${project.name}: Using buildkonfig.flavor=$buildFlavor")
        }
    }
}