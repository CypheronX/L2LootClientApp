package com.l2loot.data.repository

import com.l2loot.Config
import com.l2loot.data.networking.get
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.model.UpdateInfo
import com.l2loot.domain.repository.UpdateCheckerRepository
import com.l2loot.domain.util.Result
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class UpdateCheckerRepositoryImpl(
    private val httpClient: HttpClient,
    private val logger: LootLogger
) : UpdateCheckerRepository {
    private val githubRepo: String = Config.GITHUB_RELEASE_REPO
    private val githubToken: String = Config.GITHUB_TOKEN
    
    override suspend fun checkForUpdate(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        val result = httpClient.get<GitHubRelease>(
            route = "https://api.github.com/repos/$githubRepo/releases/latest"
        ) {
            header("Accept", "application/vnd.github.v3+json")
            
            if (githubToken.isNotEmpty()) {
                header("Authorization", "Bearer $githubToken")
            }
        }
        
        when (result) {
            is Result.Success -> {
                val release = result.data
                
                // Remove 'v' prefix from tag if present (v1.0.0 -> 1.0.0)
                val latestVersion = release.tag_name.removePrefix("v")
                
                // Skip pre-release versions (alpha, beta, test, etc.)
                if (latestVersion.contains("-")) {
                    logger.debug("Skipping pre-release version: $latestVersion")
                    return@withContext null
                }
                
                // Compare versions
                if (isNewerVersion(latestVersion, currentVersion)) {
                    // Find the MSI asset
                    val msiAsset = release.assets.find { it.name.endsWith(".msi") }
                    
                    // Find the update ZIP asset
                    val zipAsset = release.assets.find { 
                        it.name.contains("-Update-", ignoreCase = true) && it.name.endsWith(".zip", ignoreCase = true)
                    } ?: release.assets.find { it.name.endsWith(".zip", ignoreCase = true) }
                    
                    return@withContext UpdateInfo(
                        version = latestVersion,
                        downloadUrl = msiAsset?.browser_download_url ?: release.html_url,
                        releaseUrl = release.html_url,
                        releaseNotes = release.body ?: "No release notes available",
                        updateZipUrl = zipAsset?.browser_download_url ?: ""
                    )
                }
                
                null
            }
            is Result.Failure -> {
                logger.debug("Failed to check for updates: ${result.error}")
                null
            }
        }
    }
    
    /**
     * Compare semantic versions with pre-release support (e.g., "1.2.3-test" vs "1.2.0")
     * Returns true if newVersion > currentVersion
     * 
     * Supports formats like:
     * - 1.0.0
     * - 1.0.0-test
     * - 1.0.0-alpha.1
     * - 1.0.0-beta.2
     * 
     * According to semver: 1.0.0-alpha < 1.0.0-beta < 1.0.0
     */
    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        try {
            val newSemVer = parseVersion(newVersion)
            val currentSemVer = parseVersion(currentVersion)
            
            if (newSemVer.major != currentSemVer.major) {
                return newSemVer.major > currentSemVer.major
            }
            if (newSemVer.minor != currentSemVer.minor) {
                return newSemVer.minor > currentSemVer.minor
            }
            if (newSemVer.patch != currentSemVer.patch) {
                return newSemVer.patch > currentSemVer.patch
            }
            
            return currentSemVer.preRelease != null
        } catch (e: Exception) {
            logger.warn("Error comparing versions: ${e.message}")
            return false
        }
    }
    
    private fun parseVersion(version: String): SemanticVersion {
        val parts = version.split("-", limit = 2)
        val versionPart = parts[0]
        val preRelease = parts.getOrNull(1)
        
        val versionNumbers = versionPart.split(".")
        val major = versionNumbers.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = versionNumbers.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = versionNumbers.getOrNull(2)?.toIntOrNull() ?: 0
        
        return SemanticVersion(major, minor, patch, preRelease)
    }
    
    private data class SemanticVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val preRelease: String?
    )
}

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val html_url: String,
    val body: String? = null,
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    val name: String,
    val browser_download_url: String
)

