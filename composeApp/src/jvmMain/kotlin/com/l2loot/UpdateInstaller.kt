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
            
            val updaterExe = extractUpdaterToTemp()
            
            // The executable must be run from its own directory to find .cfg and runtime
            val updaterDir = updaterExe.parentFile
            
            logger.info("Launching updater: ${updaterExe.absolutePath}")
            logger.info("Working directory: ${updaterDir.absolutePath}")
            
            val githubRepo = Config.GITHUB_RELEASE_REPO
            val checkUpdateUrl = "https://api.github.com/repos/$githubRepo/releases/latest"
            
            val command = buildList {
                add(updaterExe.absolutePath)
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
                .directory(updaterDir)
                .start()
            
            logger.info("Updater launched successfully. Exiting main app.")
            
            exitProcess(0)
            
        } catch (e: Exception) {
            logger.error("Failed to launch updater: ${e.message}", e)
            throw UpdateInstallerException("Failed to launch updater", e)
        }
    }
    
    private fun extractUpdaterToTemp(): File {
        val appName = Config.APP_NAME
        val appDataDir = File(System.getProperty("user.home"), "AppData/Local/$appName/updater")
        appDataDir.mkdirs()
        
        val updaterDir = File(appDataDir, "app")
        val updaterExe = File(updaterDir, "L2Loot Updater/L2Loot Updater.exe")
        val versionMarker = File(appDataDir, "version.txt")
        val currentAppVersion = Config.VERSION_NAME
        
        val needsExtraction = when {
            !updaterExe.exists() -> true
            !versionMarker.exists() -> true
            else -> {
                val storedVersion = versionMarker.readText().trim()
                storedVersion != currentAppVersion
            }
        }
        
        if (needsExtraction) {
            if (updaterDir.exists()) {
                updaterDir.deleteRecursively()
            }
            val updaterZip = File(appDataDir, "L2LootUpdater.zip")
            
            // Try multiple resource paths for the ZIP file
            val possiblePaths = listOf(
                "l2loot.composeapp.generated.resources/files/updater/L2LootUpdater.zip",
                "/l2loot.composeapp.generated.resources/files/updater/L2LootUpdater.zip",
                "composeResources/l2loot.composeapp.generated.resources/files/updater/L2LootUpdater.zip",
                "/composeResources/l2loot.composeapp.generated.resources/files/updater/L2LootUpdater.zip",
                "files/updater/L2LootUpdater.zip",
                "/files/updater/L2LootUpdater.zip"
            )
            
            var found = false
            for (path in possiblePaths) {
                val resourceStream = javaClass.classLoader.getResourceAsStream(path)
                if (resourceStream != null) {
                    resourceStream.use { input ->
                        Files.copy(input, updaterZip.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    }
                    found = true
                    break
                }
            }
            
            if (!found) {
                throw UpdateInstallerException("Updater ZIP not found in resources")
            }
            
            java.util.zip.ZipFile(updaterZip).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val destFile = File(updaterDir, entry.name)
                    if (entry.isDirectory) {
                        destFile.mkdirs()
                    } else {
                        destFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            Files.copy(input, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                        if (destFile.name.endsWith(".exe") || destFile.name.endsWith(".sh")) {
                            destFile.setExecutable(true, false)
                        }
                    }
                }
            }
            
            updaterZip.delete()
            versionMarker.writeText(currentAppVersion)
        }
        
        val updaterAppDir = File(updaterDir, "L2Loot Updater")
        val extractedExe = File(updaterAppDir, "L2Loot Updater.exe")
        
        if (!extractedExe.exists()) {
            val exeFiles = updaterDir.walkTopDown().filter { it.isFile && it.name.endsWith(".exe") }.toList()
            if (exeFiles.isNotEmpty()) {
                return exeFiles[0]
            }
            throw UpdateInstallerException("Updater executable not found after extraction")
        }
        
        return extractedExe
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
    
}

/**
 * Exception thrown when updater installation fails
 */
class UpdateInstallerException(message: String, cause: Throwable? = null) : Exception(message, cause)

