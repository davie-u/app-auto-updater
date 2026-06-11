package com.example.appautoupdater.network

import retrofit2.http.GET
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

interface GitHubService {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Release
}

data class Release(
    val id: Long,
    @SerializedName("tag_name")
    val tagName: String,
    val name: String,
    val body: String,
    val assets: List<Asset>,
    @SerializedName("published_at")
    val publishedAt: String
)

data class Asset(
    val id: Long,
    val name: String,
    val size: Long,
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String
)
