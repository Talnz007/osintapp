package com.noobdevs.osint
 
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
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

fun MainViewController(): UIViewController {
    AppLogger.log("INIT", "MainViewController() called by iOS runner")

    setUnhandledExceptionHook { throwable ->
        AppLogger.logCrash(throwable)
    }

    return ComposeUIViewController {
        var fatalError by remember { mutableStateOf<String?>(null) }

        if (fatalError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "⚠️ OSINT Diagnostics Screen",
                        color = Color(0xFFEF4444),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "A startup error was trapped safely. Check On My iPhone -> OSINT -> launch_log.txt in the Files app.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = fatalError ?: "Unknown error",
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        } else {
            val storeOwner = remember { IosViewModelStoreOwner() }
            CompositionLocalProvider(LocalViewModelStoreOwner provides storeOwner) {
                try {
                    App()
                } catch (t: Throwable) {
                    AppLogger.logCrash(t)
                    fatalError = "${t::class.simpleName}: ${t.message}\n${t.stackTraceToString()}"
                }
            }
        }
    }
}

