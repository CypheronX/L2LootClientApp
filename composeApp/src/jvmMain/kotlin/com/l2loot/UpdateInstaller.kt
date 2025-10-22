package com.l2loot

import com.l2loot.domain.logging.LootLogger
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.system.exitProcess

/**
 * Handles launching the updater application and exiting the main app
 */
class UpdateInstaller(
    private val logger: LootLogger
) {
    /**
     * Launch updater in check mode - it will check for updates and launch main app if none found
     * This should be called at app startup
     */
    fun launchUpdaterAndCheckForUpdates() {
        try {
            logger.info("Launching updater to check for updates...")
            
            val appPath = getAppInstallationPath()
            
            val exeName = when (Config.BUILD_FLAVOR) {
                "prod" -> "L2Loot.exe"
                "stage" -> "L2Loot Stage.exe"
                else -> "L2Loot Dev.exe"
            }
            val appExePath = File(appPath, exeName).absolutePath
            
            val updaterJar = extractUpdaterToTemp()
            
            val javaExe = findBundledJava()
            
            logger.info("Launching updater: ${updaterJar.absolutePath}")
            logger.info("Using Java: $javaExe")
            
            val githubRepo = Config.GITHUB_RELEASE_REPO
            val checkUpdateUrl = "https://api.github.com/repos/$githubRepo/releases/latest"
            
            val command = buildList {
                add(javaExe)
                add("-jar")
                add(updaterJar.absolutePath)
                add("--check-update-url")
                add(checkUpdateUrl)
                add("--install-path")
                add(appPath.absolutePath)
                add("--app-exe")
                add(appExePath)
                add("--current-version")
                add(Config.VERSION_NAME)
                add("--new-version")
                add("Unknown")
                
                if (Config.GITHUB_TOKEN.isNotEmpty()) {
                    add("--github-token")
                    add(Config.GITHUB_TOKEN)
                }
            }
            
            ProcessBuilder(command)
                .directory(updaterJar.parentFile)
                .start()
            
            logger.info("Updater launched successfully. Exiting main app.")
            
            exitProcess(0)
            
        } catch (e: Exception) {
            logger.error("Failed to launch updater: ${e.message}", e)
            throw UpdateInstallerException("Failed to launch updater", e)
        }
    }
    
    /**
     * Extract updater JAR from app resources to temp directory
     */
    private fun extractUpdaterToTemp(): File {
        val tempDir = Files.createTempDirectory("l2loot-updater").toFile()
        val updaterJar = File(tempDir, "L2LootUpdater.jar")
        
        // Try multiple resource paths
        val possiblePaths = listOf(
            "l2loot.composeapp.generated.resources/files/updater/L2LootUpdater.jar",
            "/l2loot.composeapp.generated.resources/files/updater/L2LootUpdater.jar",
            "composeResources/l2loot.composeapp.generated.resources/files/updater/L2LootUpdater.jar",
            "/composeResources/l2loot.composeapp.generated.resources/files/updater/L2LootUpdater.jar",
            "files/updater/L2LootUpdater.jar",
            "/files/updater/L2LootUpdater.jar"
        )
        
        logger.debug("Searching for updater JAR in resources...")
        for (path in possiblePaths) {
            logger.debug("Trying path: $path")
            val resourceStream = javaClass.classLoader.getResourceAsStream(path)
            if (resourceStream != null) {
                logger.info("Found updater at: $path")
                resourceStream.use { input ->
                    Files.copy(input, updaterJar.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
                logger.info("Updater extracted to: ${updaterJar.absolutePath}")
                return updaterJar
            }
        }
        
        logger.error("Failed to find updater JAR in any known path")
        throw UpdateInstallerException("Updater JAR not found in resources")
    }
    
    /**
     * Get the application installation directory
     * This is the directory where the app's executable and runtime files are located
     */
    private fun getAppInstallationPath(): File {
        // Get the location of the current JAR or app directory
        val jarLocation = File(
            UpdateInstaller::class.java.protectionDomain.codeSource.location.toURI()
        )
        
        // Navigate up to find the app root directory
        // The structure is typically: AppDir/app/lib/*.jar
        var currentDir = jarLocation.parentFile
        
        // Go up from lib directory
        if (currentDir.name == "lib") {
            currentDir = currentDir.parentFile
        }
        
        // Go up from app directory if needed
        if (currentDir.name == "app") {
            currentDir = currentDir.parentFile
        }
        
        logger.debug("App installation path: ${currentDir.absolutePath}")
        
        return currentDir
    }
    
    /**
     * Find the bundled Java executable from the app's runtime
     * Returns the path to java.exe in the bundled JVM
     */
    private fun findBundledJava(): String {
        val appPath = getAppInstallationPath()
        
        // Check common locations for bundled JVM
        val possibleLocations = listOf(
            File(appPath, "runtime/bin/java.exe"),           // Windows runtime location
            File(appPath, "app/runtime/bin/java.exe"),       // Alternative location
            File(appPath, "../runtime/bin/java.exe")         // Relative location
        )
        
        for (javaExe in possibleLocations) {
            if (javaExe.exists()) {
                logger.info("Found bundled Java at: ${javaExe.absolutePath}")
                return javaExe.absolutePath
            }
        }
        
        // Fallback to system Java if bundled not found
        logger.warn("Bundled Java not found, using system Java")
        return "java"
    }
}

/**
 * Exception thrown when updater installation fails
 */
class UpdateInstallerException(message: String, cause: Throwable? = null) : Exception(message, cause)

