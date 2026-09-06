package com.noobdevs.osint.platform

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.noobdevs.osint.data.models.ThreatMapMarker
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import platform.Foundation.*
import platform.WebKit.*
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformThreatMapView(
    markers: List<ThreatMapMarker>,
    onMarkerClicked: (Long) -> Unit,
    modifier: Modifier,
    centerTrigger: Int
) {
    var webViewRef by remember { mutableStateOf<WKWebView?>(null) }

    fun sendMarkers(view: WKWebView?, list: List<ThreatMapMarker>) {
        val json = buildJsonArray {
            list.forEach { m ->
                add(buildJsonObject {
                    put("id", m.id)
                    put("lat", m.latitude)
                    put("lng", m.longitude)
                    put("title", m.title)
                    put("district", m.district)
                    put("theater", m.theater.shortCode)
                    put("activity", m.activityType.shortCode)
                    put("date", m.date)
                })
            }
        }.toString()
        view?.evaluateJavaScript("if (window.renderMarkers) { window.renderMarkers($json); }", null)
    }

    LaunchedEffect(markers) {
        webViewRef?.let { sendMarkers(it, markers) }
    }

    LaunchedEffect(centerTrigger) {
        if (centerTrigger > 0) {
            webViewRef?.evaluateJavaScript("if (window.resetView) { window.resetView(); }", null)
        }
    }

    UIKitView(
        factory = {
            val config = WKWebViewConfiguration()
            val contentController = WKUserContentController()
            contentController.addScriptMessageHandler(
                object : NSObject(), WKScriptMessageHandlerProtocol {
                    override fun userContentController(
                        userContentController: WKUserContentController,
                        didReceiveScriptMessage: WKScriptMessage
                    ) {
                        val body = didReceiveScriptMessage.body
                        val id = (body as? NSNumber)?.longValue ?: return
                        onMarkerClicked(id)
                    }
                },
                "onMarkerClicked"
            )
            config.userContentController = contentController

            val webView = WKWebView(frame = platform.CoreGraphics.CGRectZero.readValue(), configuration = config)
            val bundle = NSBundle.mainBundle
            val htmlPath = bundle.pathForResource("map", ofType = "html", inDirectory = "leaflet")
                ?: bundle.pathForResource("map", ofType = "html")

            if (htmlPath != null) {
                val url = NSURL.fileURLWithPath(htmlPath)
                webView.loadFileURL(url, allowingReadAccessToURL = url.URLByDeletingLastPathComponent ?: url)
            } else {
                val dir = bundle.resourcePath
                val fileUrl = NSURL.fileURLWithPath("$dir/leaflet/map.html")
                webView.loadFileURL(fileUrl, allowingReadAccessToURL = fileUrl.URLByDeletingLastPathComponent ?: fileUrl)
            }
            webViewRef = webView
            webView
        },
        update = { view ->
            webViewRef = view
            sendMarkers(view, markers)
        },
        modifier = modifier
    )
}
