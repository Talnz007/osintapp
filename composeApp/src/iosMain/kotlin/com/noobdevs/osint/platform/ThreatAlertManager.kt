package com.noobdevs.osint.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual object ThreatAlertManager {

    actual fun scheduleBackgroundSync() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ ->
            // Background notification permissions granted
        }
    }

    actual fun cancelBackgroundSync() {
        UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
    }

    actual fun triggerNotification(title: String, body: String, postId: Long) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent().apply {
            setTitle("🚨 $title")
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
        }

        // Trigger in 1 second
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, repeats = false)
        val id = if (postId != 0L) "alert_$postId" else "alert_${NSDate().timeIntervalSince1970}"
        val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)

        center.addNotificationRequest(request) { _ -> }
    }
}
