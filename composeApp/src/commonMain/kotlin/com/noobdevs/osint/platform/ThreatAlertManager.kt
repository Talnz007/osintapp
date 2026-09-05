package com.noobdevs.osint.platform

expect object ThreatAlertManager {
    fun scheduleBackgroundSync()
    fun cancelBackgroundSync()
    fun triggerNotification(title: String, body: String, postId: Long = 0L)
}
