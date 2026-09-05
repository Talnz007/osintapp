package com.noobdevs.osint.platform

import com.noobdevs.osint.util.IntelSyncWorker
import com.noobdevs.osint.util.NotificationHelper

actual object ThreatAlertManager {
    actual fun scheduleBackgroundSync() {
        IntelSyncWorker.schedulePeriodicSync(AppContextProvider.context)
    }

    actual fun cancelBackgroundSync() {
        IntelSyncWorker.cancelPeriodicSync(AppContextProvider.context)
    }

    actual fun triggerNotification(title: String, body: String, postId: Long) {
        NotificationHelper.showIncidentNotification(
            AppContextProvider.context,
            title = title,
            body = body,
            postId = if (postId != 0L) postId else System.currentTimeMillis()
        )
    }
}
