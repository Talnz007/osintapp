package com.noobdevs.osint
 
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlin.experimental.ExperimentalNativeApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.closeFile
import platform.Foundation.dataUsingEncoding
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.seekToEndOfFile
import platform.Foundation.writeData
import platform.Foundation.writeToFile
import platform.UIKit.UIViewController
import kotlin.native.setUnhandledExceptionHook

private class IosViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

@OptIn(ExperimentalForeignApi::class)
object AppLogger {
    private val docsDir: String? by lazy {
        try {
            val fileManager = NSFileManager.defaultManager
            val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
            val docUrl = urls.firstOrNull() as? NSURL
            docUrl?.path
        } catch (_: Throwable) {
            null
        }
    }

    fun log(tag: String, message: String) {
        val timestamp = NSDate().description
        val line = "[$timestamp][$tag] $message\n"
        println(line)

        val dir = docsDir ?: return
        val path = "$dir/launch_log.txt"
        try {
            val fileManager = NSFileManager.defaultManager
            if (!fileManager.fileExistsAtPath(path)) {
                fileManager.createFileAtPath(path, contents = null, attributes = null)
            }
            val handle = NSFileHandle.fileHandleForWritingAtPath(path)
            handle?.seekToEndOfFile()
            val data = (line as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            if (data != null) {
                handle?.writeData(data)
            }
            handle?.closeFile()
        } catch (_: Throwable) {}
    }

    fun logCrash(throwable: Throwable) {
        val timestamp = NSDate().description
        val stack = throwable.stackTraceToString()
        val text = "=== FATAL CRASH: $timestamp ===\n${throwable::class.simpleName}: ${throwable.message}\n$stack\n\n"
        log("FATAL", text)

        val dir = docsDir ?: return
        val path = "$dir/crash_log.txt"
        try {
            (text as NSString).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        } catch (_: Throwable) {}
    }
}

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    AppLogger.log("INIT", "MainViewController() called by iOS runner")

    setUnhandledExceptionHook { throwable ->
        AppLogger.logCrash(throwable)
    }

    return ComposeUIViewController {
        AppLogger.log("COMPOSE", "Rendering Compose root")
        val storeOwner = remember { IosViewModelStoreOwner() }
        CompositionLocalProvider(LocalViewModelStoreOwner provides storeOwner) {
            App()
        }
    }
}


