package com.noobdevs.osint.data

import com.russhwolf.settings.Settings
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AuthManager(private val settings: Settings = Settings()) {

    companion object {
        const val DEFAULT_BASE_URL = "https://incl-atom-worship-fishing.trycloudflare.com"
        const val DEFAULT_USERNAME = "CANTTHINKOFNTHIN"
        const val DEFAULT_PASSWORD = "NOTFASCIST"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFICATION_INTERVAL = "notification_interval"
    }

    var isDarkMode: Boolean
        get() = settings.getBoolean(KEY_DARK_MODE, false)
        set(value) { settings.putBoolean(KEY_DARK_MODE, value) }

    var notificationsEnabled: Boolean
        get() = settings.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) { settings.putBoolean(KEY_NOTIFICATIONS_ENABLED, value) }

    var notificationIntervalMinutes: Long
        get() = settings.getLong(KEY_NOTIFICATION_INTERVAL, 15L)
        set(value) { settings.putLong(KEY_NOTIFICATION_INTERVAL, value) }

    var baseUrl: String
        get() = settings.getString(KEY_BASE_URL, DEFAULT_BASE_URL).trimEnd('/')
        set(value) { settings.putString(KEY_BASE_URL, value.trimEnd('/')) }

    var username: String
        get() = settings.getString(KEY_USERNAME, DEFAULT_USERNAME)
        set(value) { settings.putString(KEY_USERNAME, value) }

    var password: String
        get() = settings.getString(KEY_PASSWORD, DEFAULT_PASSWORD)
        set(value) { settings.putString(KEY_PASSWORD, value) }

    var isLoggedIn: Boolean
        get() = settings.getBoolean(KEY_IS_LOGGED_IN, true)
        set(value) { settings.putBoolean(KEY_IS_LOGGED_IN, value) }

    @OptIn(ExperimentalEncodingApi::class)
    fun getBasicAuthHeader(): String {
        val creds = "$username:$password"
        val encoded = Base64.encode(creds.encodeToByteArray())
        return "Basic $encoded"
    }

    fun saveCredentials(url: String, user: String, pass: String) {
        baseUrl = url
        username = user
        password = pass
        isLoggedIn = true
    }

    fun logout() {
        isLoggedIn = false
    }
}
