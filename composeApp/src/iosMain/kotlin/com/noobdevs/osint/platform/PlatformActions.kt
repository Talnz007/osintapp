package com.noobdevs.osint.platform

import io.ktor.http.encodeURLParameter
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual object PlatformActions {

    actual fun shareText(text: String, title: String) {
        val rootViewController = getRootViewController() ?: return
        val activityViewController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )
        rootViewController.presentViewController(activityViewController, animated = true, completion = null)
    }

    actual fun sendWhatsApp(text: String) {
        val encodedText = text.encodeURLParameter()
        val whatsappUrl = NSURL.URLWithString("whatsapp://send?text=$encodedText")
        if (whatsappUrl != null && UIApplication.sharedApplication.canOpenURL(whatsappUrl)) {
            UIApplication.sharedApplication.openURL(whatsappUrl)
        } else {
            shareText(text, "Share via WhatsApp")
        }
    }

    actual fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    }

    actual fun openPostInXApp(url: String) {
        if (url.isBlank()) return
        val tweetId = Regex("""status/(\d+)""").find(url)?.groupValues?.getOrNull(1)
        if (tweetId != null) {
            val twitterUrl = NSURL.URLWithString("twitter://status?status_id=$tweetId")
            if (twitterUrl != null && UIApplication.sharedApplication.canOpenURL(twitterUrl)) {
                UIApplication.sharedApplication.openURL(twitterUrl)
                return
            }
        }
        openUrl(url)
    }

    actual fun copyToClipboard(text: String, label: String) {
        UIPasteboard.generalPasteboard.string = text
    }

    actual fun saveFileToDownloads(filename: String, bytes: ByteArray): Result<String> {
        return try {
            val fileManager = NSFileManager.defaultManager
            val documentsUrl = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first() as NSURL
            val fileUrl = documentsUrl.URLByAppendingPathComponent(filename) ?: return Result.failure(Exception("Invalid file URL"))

            val data = if (bytes.isNotEmpty()) {
                bytes.usePinned { pinned ->
                    NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
                }
            } else {
                NSData()
            }
            data.writeToURL(fileUrl, atomically = true)
            Result.success(fileUrl.path ?: filename)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getRootViewController(): UIViewController? {
        val window = UIApplication.sharedApplication.keyWindow
            ?: UIApplication.sharedApplication.windows.firstOrNull() as? platform.UIKit.UIWindow
        return window?.rootViewController
    }
}
