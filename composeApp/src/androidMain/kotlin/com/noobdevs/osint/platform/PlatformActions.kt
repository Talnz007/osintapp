package com.noobdevs.osint.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

actual object PlatformActions {

    actual fun shareText(text: String, title: String) {
        val ctx = AppContextProvider.context
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, title).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ctx.startActivity(chooser)
    }

    actual fun sendWhatsApp(text: String) {
        val ctx = AppContextProvider.context
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            ctx.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(fallback, "Send via WhatsApp or other app...").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(chooser)
        }
    }

    actual fun openUrl(url: String) {
        val ctx = AppContextProvider.context
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(intent)
        } catch (_: Exception) {}
    }

    actual fun openPostInXApp(url: String) {
        if (url.isBlank()) return
        val ctx = AppContextProvider.context
        val tweetId = Regex("""status/(\d+)""").find(url)?.groupValues?.getOrNull(1)
        if (tweetId != null) {
            try {
                val twitterAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse("twitter://status?status_id=$tweetId")).apply {
                    setPackage("com.twitter.android")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(twitterAppIntent)
                return
            } catch (_: Exception) {
                try {
                    val genericTwitterIntent = Intent(Intent.ACTION_VIEW, Uri.parse("twitter://status?status_id=$tweetId")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    ctx.startActivity(genericTwitterIntent)
                    return
                } catch (_: Exception) {
                    try {
                        val httpTwitterIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/i/status/$tweetId")).apply {
                            setPackage("com.twitter.android")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        ctx.startActivity(httpTwitterIntent)
                        return
                    } catch (_: Exception) {}
                }
            }
        }
        openUrl(url)
    }

    actual fun copyToClipboard(text: String, label: String) {
        val ctx = AppContextProvider.context
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    actual fun saveFileToDownloads(filename: String, bytes: ByteArray): Result<String> {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, filename)
            FileOutputStream(file).use { it.write(bytes) }
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
