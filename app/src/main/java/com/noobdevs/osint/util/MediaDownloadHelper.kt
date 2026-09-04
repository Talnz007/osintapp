package com.noobdevs.osint.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.noobdevs.osint.data.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object MediaDownloadHelper {

    fun downloadDeckWithManager(context: Context, authManager: AuthManager, filename: String) {
        try {
            val downloadUrl = "${authManager.baseUrl}/api/deck/download?filename=${Uri.encode(filename)}"
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("OSINT Briefing: $filename")
                .setDescription("Downloading PowerPoint intelligence deck")
                .addRequestHeader("Authorization", authManager.getBasicAuthHeader())
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(context, "Downloading $filename to Downloads...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun downloadImageFile(
        context: Context,
        okHttpClient: OkHttpClient,
        imageUrl: String,
        suggestedFileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(imageUrl).get().build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outputFile = File(downloadsDir, suggestedFileName)
            response.body?.byteStream()?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openUrlInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openVideoDownloaderOrTweet(context: Context, tweetUrl: String) {
        try {
            // Open tweet or allow user to download video through external tools
            val cleanUrl = tweetUrl.replace("x.com", "twitter.com")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open video link", Toast.LENGTH_SHORT).show()
        }
    }
}
