package com.example.appautoupdater

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.appautoupdater.network.GitHubService
import com.example.appautoupdater.repository.UpdateRepository
import com.example.appautoupdater.ui.theme.AppUpdateManagerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var updateRepository: UpdateRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Retrofit with secure HTTPS configuration
        val githubService = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
        
        updateRepository = UpdateRepository(this, githubService)
        
        setContent {
            AppUpdateManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UpdateScreen(
                        updateRepository = updateRepository,
                        onInstallRequested = { file -> installApk(file) }
                    )
                }
            }
        }
    }

    /**
     * Install the downloaded APK using FileProvider for security
     */
    private fun installApk(file: File) {
        if (!file.exists()) return
        
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun UpdateScreen(
    updateRepository: UpdateRepository,
    onInstallRequested: (File) -> Unit
) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Update System Ready") }
    var progress by remember { mutableStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (isDownloading) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.width(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("${(progress * 100).toInt()}%")
            
            Button(
                onClick = {
                    isDownloading = false
                    status = "Update Cancelled"
                }
            ) {
                Text("Cancel")
            }
        } else {
            Button(
                onClick = {
                    isDownloading = true
                    status = "Checking for updates..."
                    errorMessage = ""
                    showError = false
                    
                    scope.launch {
                        try {
                            // Fetch latest release
                            val releaseResult = updateRepository.getLatestRelease()
                            
                            releaseResult.onSuccess { release ->
                                val apkAsset = release.assets.find { it.name.endsWith(".apk") }
                                
                                if (apkAsset != null) {
                                    status = "Downloading patch..."
                                    
                                    // Download with progress tracking
                                    val downloadResult = updateRepository.downloadApk(
                                        apkAsset.browserDownloadUrl
                                    ) { current, total ->
                                        progress = if (total > 0) current.toFloat() / total else 0f
                                    }
                                    
                                    downloadResult.onSuccess { file ->
                                        status = "Verifying APK..."
                                        
                                        // Verify checksum if available
                                        val verifyResult = updateRepository.verifyApk(file, null)
                                        
                                        verifyResult.onSuccess {
                                            status = "Download complete! Installing..."
                                            onInstallRequested(file)
                                        }
                                        
                                        verifyResult.onFailure { error ->
                                            status = "Verification failed"
                                            errorMessage = error.message ?: "Unknown error"
                                            showError = true
                                            isDownloading = false
                                        }
                                    }
                                    
                                    downloadResult.onFailure { error ->
                                        status = "Download failed"
                                        errorMessage = error.message ?: "Network error"
                                        showError = true
                                        isDownloading = false
                                    }
                                } else {
                                    status = "No APK found in latest release"
                                    errorMessage = "The latest release does not contain an APK file"
                                    showError = true
                                    isDownloading = false
                                }
                            }
                            
                            releaseResult.onFailure { error ->
                                status = "Failed to check updates"
                                errorMessage = error.message ?: "Unknown error"
                                showError = true
                                isDownloading = false
                            }
                        } catch (e: Exception) {
                            status = "Error"
                            errorMessage = e.message ?: "Unknown error occurred"
                            showError = true
                            isDownloading = false
                        }
                    }
                },
                enabled = !isDownloading
            ) {
                Text("Check for Updates")
            }
        }
        
        if (showError) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Error: $errorMessage",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
