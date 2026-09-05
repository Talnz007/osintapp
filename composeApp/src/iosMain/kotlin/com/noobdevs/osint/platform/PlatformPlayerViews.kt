package com.noobdevs.osint.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformVideoPlayer(url: String, modifier: Modifier) {
    val nsUrl = remember(url) { NSURL.URLWithString(url) }
    if (nsUrl != null) {
        UIKitView(
            factory = {
                val player = AVPlayer(uRL = nsUrl)
                val playerController = AVPlayerViewController().apply {
                    this.player = player
                    this.showsPlaybackControls = true
                }
                player.play()
                playerController.view
            },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(url: String, modifier: Modifier) {
    val nsUrl = remember(url) { NSURL.URLWithString(url) }
    if (nsUrl != null) {
        UIKitView(
            factory = {
                val webView = WKWebView()
                val request = NSURLRequest.requestWithURL(nsUrl)
                webView.loadRequest(request)
                webView
            },
            modifier = modifier
        )
    }
}
