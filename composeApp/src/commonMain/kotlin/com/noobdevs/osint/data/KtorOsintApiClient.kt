package com.noobdevs.osint.data

import com.noobdevs.osint.data.models.DeckListResponse
import com.noobdevs.osint.data.models.PostsResponse
import com.noobdevs.osint.data.models.StatsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class KtorOsintApiClient(
    val authManager: AuthManager,
    val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 60_000
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
) {
    val gitHubBackup: GitHubBackupDataSource = GitHubBackupDataSource(client)
    private val _isBackupMirrorActive = MutableStateFlow(false)
    val isBackupMirrorActive: StateFlow<Boolean> = _isBackupMirrorActive.asStateFlow()

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun testLogin(url: String, user: String, pass: String): Result<String> {
        return try {
            val cleanUrl = url.trimEnd('/')
            val creds = "$user:$pass"
            val authHeader = "Basic ${Base64.encode(creds.encodeToByteArray())}"

            val response = client.post("$cleanUrl/api/login") {
                header("Authorization", authHeader)
                header("Accept", "application/json")
                contentType(ContentType.Application.Json)
                setBody("{}")
            }

            if (response.status.isSuccess()) {
                Result.success("Connected successfully as $user")
            } else {
                val body = response.bodyAsText()
                Result.failure(Exception("Server error ${response.status.value}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testGitHubMirror(): Result<String> {
        return gitHubBackup.fetchReports(forceRefresh = true).map {
            "Connected to GitHub Backup Mirror (${it.size} reports synced)"
        }
    }

    suspend fun getStats(): Result<StatsResponse> {
        val primaryResult = try {
            val response = client.get("${authManager.baseUrl}/api/stats") {
                header("Authorization", authManager.getBasicAuthHeader())
                header("Accept", "application/json")
            }

            if (response.status.isSuccess()) {
                _isBackupMirrorActive.value = false
                Result.success(response.body<StatsResponse>())
            } else {
                val body = response.bodyAsText()
                Result.failure(Exception("HTTP ${response.status.value}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

        if (primaryResult.isSuccess) {
            return primaryResult
        }

        // Automatic emergency failover to GitHub Backup Mirror
        _isBackupMirrorActive.value = true
        return gitHubBackup.getStats()
    }

    suspend fun getPosts(
        page: Int = 1,
        limit: Int = 20,
        category: String? = null,
        attackType: String? = null,
        search: String? = null,
        pipeline: String? = null,
        hours: Int? = null
    ): Result<PostsResponse> {
        val primaryResult = try {
            val response = client.get("${authManager.baseUrl}/api/posts") {
                header("Authorization", authManager.getBasicAuthHeader())
                header("Accept", "application/json")
                parameter("page", page)
                parameter("limit", limit)
                if (!category.isNullOrBlank() && category != "ALL") {
                    parameter("category", category)
                }
                if (!attackType.isNullOrBlank() && attackType != "ALL") {
                    parameter("attack_type", attackType)
                }
                if (!search.isNullOrBlank()) {
                    parameter("search", search)
                }
                if (!pipeline.isNullOrBlank()) {
                    parameter("pipeline", pipeline)
                }
                if (hours != null) {
                    parameter("hours", hours)
                }
            }

            if (response.status.isSuccess()) {
                _isBackupMirrorActive.value = false
                Result.success(response.body<PostsResponse>())
            } else {
                val body = response.bodyAsText()
                Result.failure(Exception("HTTP ${response.status.value}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

        if (primaryResult.isSuccess) {
            return primaryResult
        }

        // Automatic emergency failover to GitHub Backup Mirror
        _isBackupMirrorActive.value = true
        return gitHubBackup.getPosts(page, limit, category, attackType, search, pipeline, hours)
    }

    suspend fun getDecks(): Result<DeckListResponse> {
        return try {
            val response = client.get("${authManager.baseUrl}/api/deck/list") {
                header("Authorization", authManager.getBasicAuthHeader())
                header("Accept", "application/json")
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val body = response.bodyAsText()
                Result.failure(Exception("HTTP ${response.status.value}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMediaUrl(sentimentImagePath: String?): String? {
        if (sentimentImagePath.isNullOrBlank()) return null
        return "${authManager.baseUrl}/api/media?path=${sentimentImagePath}"
    }

    fun getDeckDownloadUrl(filename: String): String {
        return "${authManager.baseUrl}/api/deck/download?filename=${filename}"
    }

    suspend fun downloadBytes(url: String): Result<ByteArray> {
        return try {
            val response = client.get(url) {
                header("Authorization", authManager.getBasicAuthHeader())
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
