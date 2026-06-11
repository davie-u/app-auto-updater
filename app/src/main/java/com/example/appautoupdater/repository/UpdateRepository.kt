package com.example.appautoupdater.repository

import android.content.Context
import android.util.Log
import com.example.appautoupdater.network.GitHubService
import com.example.appautoupdater.network.Release
import com.example.appautoupdater.security.ApkVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

/**
 * Repository pattern for handling all update-related operations
 */
class UpdateRepository(
    private val context: Context,
    private val githubService: GitHubService
) {
    
    companion object {
        private const val TAG = "UpdateRepository"
        private const val OWNER = "davie-u"  // Replace with your GitHub username
        private const val REPO = "app-auto-updater"  // Replace with your repo name
    }
    
    /**
     * Fetch the latest release from GitHub
     */
    suspend fun getLatestRelease(): Result<Release> = withContext(Dispatchers.IO) {
        try {
            val release = githubService.getLatestRelease(OWNER, REPO)
            Log.d(TAG, "Latest release: ${release.tagName}")
            Result.success(release)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch latest release", e)
            Result.failure(e)
        }
    }
    
    /**
     * Download APK with progress tracking
     */
    suspend fun downloadApk(
        url: String,
        onProgress: (current: Long, total: Long) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        return@withContext try {
            val destination = File(
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS),
                "update_${System.currentTimeMillis()}.apk"
            )
            
            val connection = URL(url).openConnection()
            val totalSize = connection.contentLength.toLong()
            var downloadedSize = 0L
            
            connection.getInputStream().use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead
                        onProgress(downloadedSize, totalSize)
                    }
                }
            }
            
            Log.d(TAG, "APK downloaded successfully: ${destination.absolutePath}")
            Result.success(destination)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download APK", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verify downloaded APK integrity
     */
    suspend fun verifyApk(file: File, expectedChecksum: String?): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!file.exists()) {
                    return@withContext Result.failure(Exception("APK file not found"))
                }
                
                val isValid = if (expectedChecksum != null) {
                    ApkVerifier.verifyFileChecksum(file, expectedChecksum)
                } else {
                    true // Skip verification if no checksum provided
                }
                
                if (isValid) {
                    Log.d(TAG, "APK verification successful")
                    Result.success(true)
                } else {
                    Log.e(TAG, "APK verification failed - checksum mismatch")
                    file.delete()
                    Result.failure(Exception("APK checksum verification failed"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error verifying APK", e)
                Result.failure(e)
            }
        }
    }
}
