package com.example.appautoupdater

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

// This is the model you asked for earlier!
data class GitHubRelease(
    val tag_name: String, 
    val assets: List<GitHubAsset>
)

data class GitHubAsset(
    val browser_download_url: String, 
    val name: String
)

// This is the 'Phone Line' to your GitHub repo
interface GitHubService {
    @GET("repos/davie-u/app-auto-updater/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease
}
