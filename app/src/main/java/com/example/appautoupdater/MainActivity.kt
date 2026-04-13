package com.example.appautoupdater

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.appautoupdater.ui.theme.AppUpdateManagerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {private val githubService = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GitHubService::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppUpdateManagerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    UpdateScreen(onInstallRequested = { file -> installApk(file) })
                }
            }
        }
    }

    // This function talks to the Android System to launch the installer
    private fun installApk(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider", // This matches the manifest authority
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}

@Composable
fun UpdateScreen(onInstallRequested: (File) -> Unit) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Update System Ready") }
    var progress by remember { mutableStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = status, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))

        if (isDownloading) {
            LinearProgressIndicator(progress = progress, modifier = Modifier.width(200.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("${(progress * 100).toInt()}%")
        } else {
           Button(onClick = {
    isDownloading = true
    scope.launch {
        try {
            status = "Checking GitHub..."
            // This calls the service you just added at the top!
            val release = githubService.getLatestRelease() 
            
            // This looks for the first file that ends in .apk
            val apkUrl = release.assets.find { it.name.endsWith(".apk") }?.browser_download_url
            
            if (apkUrl != null) {
                status = "Update Found: ${release.tag_name}. Ready to download."
                // In the next step, we'll add the downloader!
            } else {
                status = "No APK file found in this release."
            }
        } catch (e: Exception) {
            status = "Error: Check your internet connection."
        }
        isDownloading = false
    }
}) {
    Text("Check for Patches")
}
                    
                    // This looks for the file to install (Make sure the file exists or change the path!)
                    val updateFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "update.apk")
                    onInstallRequested(updateFile)
                }
            }) {
                Text("Start Simulated Update")
            }
        }
    }
}
