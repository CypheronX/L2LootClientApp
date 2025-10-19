package com.l2loot

import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.repository.UpdateCheckerRepository
import com.l2loot.update.UpdateInstaller
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin

/**
 * Handles checking for updates on app startup (before main UI launches)
 */
class CheckForUpdatesOnStartup(private val koin: Koin) {
    
    private val updateChecker: UpdateCheckerRepository by lazy { koin.get() }
    private val logger: LootLogger by lazy { koin.get() }
    
    /**
     * Check for updates and launch updater if available
     * 
     * @return true if updater was launched (app should exit), false if no update or error
     */
    fun checkAndLaunchUpdater(): Boolean {
        return try {
            runBlocking {
                logger.info("Checking for updates on startup...")
                
                val updateInfo = updateChecker.checkForUpdate(Config.VERSION_NAME)
                
                if (updateInfo != null && updateInfo.updateZipUrl.isNotEmpty()) {
                    logger.info("Update available: ${updateInfo.version}")
                    
                    val installer = UpdateInstaller(logger)
                    installer.launchUpdaterAndExit(
                        updateZipUrl = updateInfo.updateZipUrl,
                        currentVersion = Config.VERSION_NAME,
                        newVersion = updateInfo.version
                    )
                    
                    // This line should not be reached as launchUpdaterAndExit calls exitProcess
                    true
                } else {
                    logger.info("No update available, launching app normally")
                    false
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to check for updates: ${e.message}", e)
            false
        }
    }
}

