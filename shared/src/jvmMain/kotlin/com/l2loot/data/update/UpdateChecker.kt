package com.l2loot.data.update

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Service to check for app updates from GitHub releases
 */
interface UpdateChecker {
    suspend fun checkForUpdate(currentVersion: String): UpdateInfo?
}

class UpdateCheckerImpl(
    private val githubRepo: String = "aleksbalev/L2LootClientAppReleases"
) : UpdateChecker {
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
                isLenient = true
            })
        }
    }
    
    override suspend fun checkForUpdate(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            // Fetch latest release from GitHub API
            val response = client.get("https://api.github.com/repos/$githubRepo/releases/latest") {
                header("Accept", "application/vnd.github.v3+json")
            }
            
            if (response.status != HttpStatusCode.OK) {
                println("Failed to check for updates: ${response.status}")
                return@withContext null
            }
            
            val release: GitHubRelease = response.body()
            
            // Remove 'v' prefix from tag if present (v1.0.0 -> 1.0.0)
            val latestVersion = release.tag_name.removePrefix("v")
            
            // Compare versions
            if (isNewerVersion(latestVersion, currentVersion)) {
                // Find the MSI asset
                val msiAsset = release.assets.find { it.name.endsWith(".msi") }
                
                return@withContext UpdateInfo(
                    version = latestVersion,
                    downloadUrl = msiAsset?.browser_download_url ?: release.html_url,
                    releaseUrl = release.html_url,
                    releaseNotes = release.body ?: "No release notes available"
                )
            }
            
            null
        } catch (e: Exception) {
            println("Error checking for updates: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Compare semantic versions (e.g., "1.2.3" vs "1.2.0")
     * Returns true if newVersion > currentVersion
     */
    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        try {
            val newParts = newVersion.split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
            
            val maxLength = maxOf(newParts.size, currentParts.size)
            
            for (i in 0 until maxLength) {
                val newPart = newParts.getOrNull(i) ?: 0
                val currentPart = currentParts.getOrNull(i) ?: 0
                
                if (newPart > currentPart) return true
                if (newPart < currentPart) return false
            }
            
            return false // Versions are equal
        } catch (e: Exception) {
            println("Error comparing versions: ${e.message}")
            return false
        }
    }
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

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val releaseUrl: String,
    val releaseNotes: String
)

