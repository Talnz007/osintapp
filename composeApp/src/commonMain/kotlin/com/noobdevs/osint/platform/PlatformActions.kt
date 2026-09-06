package com.noobdevs.osint.platform

expect object PlatformActions {
    fun shareText(text: String, title: String = "Share Intelligence")
    fun sendWhatsApp(text: String)
    fun openUrl(url: String)
    fun openPostInXApp(url: String)
    fun copyToClipboard(text: String, label: String = "OSINT SITREP")
    fun saveFileToDownloads(filename: String, bytes: ByteArray): Result<String>
}
