package com.noobdevs.osint.data

import android.content.Context
import android.util.Base64
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class OsintApiClient(private val authManager: AuthManager) {
    private val gson = Gson()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Authorization", authManager.getBasicAuthHeader())
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        })
        .build()

    suspend fun testLogin(url: String, user: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = url.trimEnd('/')
            val creds = "$user:$pass"
            val b64 = try {
                Base64.encodeToString(creds.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            } catch (e: Throwable) {
                java.util.Base64.getEncoder().encodeToString(creds.toByteArray(Charsets.UTF_8))
            }
            val authHeader = "Basic $b64"
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url("$cleanUrl/api/login")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .header("Authorization", authHeader)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                Result.success("Connected successfully as $user")
            } else {
                Result.failure(IOException("Server error ${response.code}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStats(): Result<StatsResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${authManager.baseUrl}/api/stats")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: throw IOException("Empty response")

            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("HTTP ${response.code}: $body"))
            }

            val stats = gson.fromJson(body, StatsResponse::class.java)
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPosts(
        page: Int = 1,
        limit: Int = 20,
        category: String? = null,
        attackType: String? = null,
        search: String? = null,
        pipeline: String? = null,
        hours: Int? = null
    ): Result<PostsResponse> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = "${authManager.baseUrl}/api/posts".toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(IOException("Invalid URL: ${authManager.baseUrl}/api/posts"))

            urlBuilder.addQueryParameter("page", page.toString())
            urlBuilder.addQueryParameter("limit", limit.toString())

            if (!category.isNullOrBlank() && category != "ALL") {
                urlBuilder.addQueryParameter("category", category)
            }
            if (!attackType.isNullOrBlank() && attackType != "ALL") {
                urlBuilder.addQueryParameter("attack_type", attackType)
            }
            if (!search.isNullOrBlank()) {
                urlBuilder.addQueryParameter("search", search)
            }
            if (!pipeline.isNullOrBlank()) {
                urlBuilder.addQueryParameter("pipeline", pipeline)
            }
            if (hours != null) {
                urlBuilder.addQueryParameter("hours", hours.toString())
            }

            val request = Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: throw IOException("Empty response")

            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("HTTP ${response.code}: $body"))
            }

            val posts = gson.fromJson(body, PostsResponse::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDecks(): Result<DeckListResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${authManager.baseUrl}/api/deck/list")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: throw IOException("Empty response")

            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("HTTP ${response.code}: $body"))
            }

            val decks = gson.fromJson(body, DeckListResponse::class.java)
            Result.success(decks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMediaUrl(sentimentImagePath: String?): String? {
        if (sentimentImagePath.isNullOrBlank()) return null
        return "${authManager.baseUrl}/api/media?path=${sentimentImagePath}"
    }

    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient { okHttpClient }
            .crossfade(true)
            .build()
    }
}
