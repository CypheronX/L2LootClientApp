package com.l2loot.update

import com.l2loot.Config
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
     * Extract updater executable from resources, launch it with arguments, and exit
     * 
     * @param updateZipUrl URL to download the update ZIP
     * @param currentVersion Current version of the app
     * @param newVersion New version to update to
     */
    fun launchUpdaterAndExit(
        updateZipUrl: String,
        currentVersion: String,
        newVersion: String
    ) {
        try {
            logger.info("Preparing to launch updater: $currentVersion -> $newVersion")
            
            // Get app installation directory
            val appPath = getAppInstallationPath()
            
            // Determine executable name based on build flavor
            val exeName = if (Config.BUILD_FLAVOR == "prod") "L2Loot.exe" else "L2Loot Dev.exe"
            val appExePath = File(appPath, exeName).absolutePath
            
            // Extract updater JAR from resources to temp directory
            val updaterJar = extractUpdaterToTemp()
            
            // Find java.exe from the bundled JVM runtime
            val javaExe = findBundledJava()
            
            logger.info("Launching updater: ${updaterJar.absolutePath}")
            logger.info("Using Java: $javaExe")
            
            // Build command arguments - run JAR with bundled JVM
            val command = listOf(
                javaExe,
                "-jar", updaterJar.absolutePath,
                "--download-url", updateZipUrl,
                "--install-path", appPath.absolutePath,
                "--app-exe", appExePath,
                "--current-version", currentVersion,
                "--new-version", newVersion
            )
            
            // Launch updater process
            ProcessBuilder(command)
                .directory(updaterJar.parentFile)
                .start()
            
            logger.info("Updater launched successfully. Exiting main app.")
            
            // Exit main app to allow updater to replace files
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
        
        // Extract from resources
        val resourceStream = javaClass.classLoader.getResourceAsStream("files/updater/L2LootUpdater.jar")
            ?: throw UpdateInstallerException("Updater JAR not found in resources")
        
        resourceStream.use { input ->
            Files.copy(input, updaterJar.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        
        logger.info("Updater extracted to: ${updaterJar.absolutePath}")
        
        return updaterJar
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

