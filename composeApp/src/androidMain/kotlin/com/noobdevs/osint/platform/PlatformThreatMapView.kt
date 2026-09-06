package com.noobdevs.osint.platform

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.noobdevs.osint.data.models.ThreatMapMarker
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformThreatMapView(
    markers: List<ThreatMapMarker>,
    onMarkerClicked: (Long) -> Unit,
    modifier: Modifier,
    centerTrigger: Int
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    fun sendMarkers(view: WebView?, list: List<ThreatMapMarker>) {
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
        view?.evaluateJavascript("if (window.renderMarkers) { window.renderMarkers($json); }", null)
    }

    LaunchedEffect(markers) {
        webViewRef?.let { sendMarkers(it, markers) }
    }

    LaunchedEffect(centerTrigger) {
        if (centerTrigger > 0) {
            webViewRef?.evaluateJavascript("if (window.resetView) { window.resetView(); }", null)
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    builtInZoomControls = false
                    displayZoomControls = false
                }
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onMarkerClicked(id: Long) {
                        onMarkerClicked(id)
                    }
                }, "AndroidBridge")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        sendMarkers(view, markers)
                    }
                }
                loadUrl("file:///android_asset/leaflet/map.html")
                webViewRef = this
            }
        },
        update = {
            webViewRef = it
            sendMarkers(it, markers)
        },
        modifier = modifier
    )
}
