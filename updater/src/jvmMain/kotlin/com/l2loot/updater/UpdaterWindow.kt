package com.l2loot.updater

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l2loot.theme.AppTheme
import com.l2loot.theme.LocalSpacing
import com.l2loot.data.repository.UpdateCheckerRepositoryImpl
import com.l2loot.data.logging.KermitLogger
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile
import kotlin.io.path.deleteIfExists

sealed class UpdateState {
    object Checking : UpdateState()
    object Downloading : UpdateState()
    object Extracting : UpdateState()
    object Installing : UpdateState()
    object Completed : UpdateState()
    object NoUpdateAvailable : UpdateState()
    data class Error(val message: String) : UpdateState()
}

@Composable
fun UpdaterWindow(
    arguments: UpdaterArguments,
    onComplete: (success: Boolean, scope: kotlinx.coroutines.CoroutineScope) -> Unit,
    circularProgressionSize: Dp = 48.dp
) {
    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Checking) }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Checking for updates...") }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                updateState = UpdateState.Checking
                statusText = "Checking for updates..."
                
                val httpClient = HttpClient(OkHttp) {
                    install(ContentNegotiation) {
                        json(Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        })
                    }
                }
                
                val updateChecker = UpdateCheckerRepositoryImpl(httpClient, KermitLogger)
                
                val updateInfo = try {
                    updateChecker.checkForUpdate(arguments.currentVersion)
                } finally {
                    httpClient.close()
                }
                
                if (updateInfo == null || updateInfo.updateZipUrl.isEmpty()) {
                    updateState = UpdateState.NoUpdateAvailable
                    statusText = "No updates available"
                    delay(500)
                    onComplete(true, scope)
                    return@launch
                }
                
                updateState = UpdateState.Downloading
                statusText = "Downloading update..."
                val zipFile = downloadUpdate(updateInfo.updateZipUrl, arguments.githubToken) { downloadProgress ->
                    progress = downloadProgress
                    statusText = "Downloading update... ${(downloadProgress * 100).toInt()}%"
                }
                
                // Extract update
                updateState = UpdateState.Extracting
                statusText = "Extracting files..."
                progress = 0f
                val extractedDir = extractZip(zipFile) { extractProgress ->
                    progress = extractProgress
                    statusText = "Extracting files... ${(extractProgress * 100).toInt()}%"
                }
                
                // Install update
                updateState = UpdateState.Installing
                statusText = "Installing update..."
                progress = 0f
                installUpdate(extractedDir, arguments.installPath) { installProgress ->
                    progress = installProgress
                    statusText = "Installing update... ${(installProgress * 100).toInt()}%"
                }
                
                // Update Windows registry version
                try {
                    updateWindowsRegistryVersion(updateInfo.version)
                } catch (e: Exception) {
                    println("Warning: Failed to update registry version: ${e.message}")
                }
                
                // Cleanup
                zipFile.toPath().deleteIfExists()
                extractedDir.deleteRecursively()
                
                // Complete
                updateState = UpdateState.Completed
                statusText = "Update completed successfully!"
                progress = 1f
                
                delay(1000)
                onComplete(true, scope)
                
            } catch (e: Exception) {
                updateState = UpdateState.Error(e.message ?: "Unknown error")
                statusText = "Update failed: ${e.message}"
            }
        }
    }

    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(LocalSpacing.current.space28),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = LocalSpacing.current.space8
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(LocalSpacing.current.space32),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "L2Loot Updater",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(LocalSpacing.current.space32))
                
                when (updateState) {
                    is UpdateState.Checking -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(circularProgressionSize),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(LocalSpacing.current.space16))
                        
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    is UpdateState.NoUpdateAvailable -> {
                        Text(
                            text = "✓ No updates available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Launching app...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    is UpdateState.Error -> {
                        Text(
                            text = statusText,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { onComplete(false, scope) }
                        ) {
                            Text("Close")
                        }
                    }
                    
                    else -> {
                        LinearProgressIndicator(
                            progress = { progress },
                            gapSize = LocalSpacing.current.none,
                            drawStopIndicator = { },
                            modifier = Modifier.padding(horizontal = LocalSpacing.current.space32)
                                .fillMaxWidth(fraction = 0.6f)
                        )
                        Spacer(modifier = Modifier.size(LocalSpacing.current.space16))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Download update ZIP file
 */
suspend fun downloadUpdate(
    url: String,
    githubToken: String?,
    onProgress: (Float) -> Unit
): File = withContext(Dispatchers.IO) {
    val tempFile = Files.createTempFile("l2loot-update", ".zip").toFile()
    
    val connection = URL(url).openConnection()
    
    if (!githubToken.isNullOrEmpty()) {
        connection.setRequestProperty("Authorization", "Bearer $githubToken")
        connection.setRequestProperty("Accept", "application/octet-stream")
    }
    
    val totalSize = connection.contentLengthLong
    
    connection.getInputStream().use { input ->
        FileOutputStream(tempFile).use { output ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L
            
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                
                if (totalSize > 0) {
                    onProgress(totalBytesRead.toFloat() / totalSize)
                }
            }
        }
    }
    
    tempFile
}

/**
 * Extract ZIP file
 */
suspend fun extractZip(
    zipFile: File,
    onProgress: (Float) -> Unit
): File = withContext(Dispatchers.IO) {
    val extractDir = Files.createTempDirectory("l2loot-extracted").toFile()
    
    ZipFile(zipFile).use { zip ->
        val entries = zip.entries().toList()
        val totalEntries = entries.size
        
        entries.forEachIndexed { index, entry ->
            val destFile = File(extractDir, entry.name)
            
            if (entry.isDirectory) {
                destFile.mkdirs()
            } else {
                destFile.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            
            onProgress((index + 1).toFloat() / totalEntries)
        }
    }
    
    extractDir
}

/**
 * Install update by replacing files
 */
suspend fun installUpdate(
    sourceDir: File,
    targetPath: String,
    onProgress: (Float) -> Unit
) = withContext(Dispatchers.IO) {
    val targetDir = File(targetPath)
    
    val filesToCopy = sourceDir.walkTopDown().filter { it.isFile }.toList()
    val totalFiles = filesToCopy.size
    
    filesToCopy.forEachIndexed { index, sourceFile ->
        val relativePath = sourceFile.relativeTo(sourceDir)
        val targetFile = File(targetDir, relativePath.path)
        
        targetFile.parentFile?.mkdirs()
        
        var retries = 3
        while (retries > 0) {
            try {
                Files.copy(
                    sourceFile.toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                break
            } catch (e: Exception) {
                retries--
                if (retries == 0) throw e
                delay(1000) // Wait before retry
            }
        }
        
        onProgress((index + 1).toFloat() / totalFiles)
    }
}

/**
 * Update Windows registry DisplayVersion to reflect the new version
 * This ensures Windows Apps & Features shows the correct version
 */
suspend fun updateWindowsRegistryVersion(newVersion: String) = withContext(Dispatchers.IO) {
    try {
        // Known upgrade UUIDs for each flavor
        val upgradeUuids = listOf(
            "a8e9c7c4-5f4d-4e8a-9c3b-8f2d1e4a5b6c", // prod
            "c0e1f9f6-7f6f-6f0c-be5d-0f4f3f6c7d8e", // stage
            "b9f0d8d5-6f5e-5f9b-ad4c-9f3e2f5b6c7d"  // dev
        )
        
        for (uuid in upgradeUuids) {
            val registryPath = "HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\$uuid"
            
            val checkProcess = ProcessBuilder(
                "reg", "query", registryPath, "/v", "DisplayVersion"
            ).redirectErrorStream(true).start()
            
            val exitCode = checkProcess.waitFor()
            
            if (exitCode == 0) {
                println("Updating registry version at: $registryPath")
                
                val updateProcess = ProcessBuilder(
                    "reg", "add", registryPath,
                    "/v", "DisplayVersion",
                    "/t", "REG_SZ",
                    "/d", newVersion,
                    "/f"
                ).redirectErrorStream(true).start()
                
                val updateResult = updateProcess.waitFor()
                
                if (updateResult == 0) {
                    println("✓ Successfully updated Windows registry version to $newVersion")
                } else {
                    println("⚠ Registry update command failed with exit code: $updateResult")
                }
                
                break
            }
        }
    } catch (e: Exception) {
        println("Error updating registry: ${e.message}")
        throw e
    }
}

