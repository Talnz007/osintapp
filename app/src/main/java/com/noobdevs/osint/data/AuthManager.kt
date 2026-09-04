package com.noobdevs.osint.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

class AuthManager(context: Context? = null) {
    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("osint_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_BASE_URL = "https://incl-atom-worship-fishing.trycloudflare.com"
        const val DEFAULT_USERNAME = "CANTTHINKOFNTHIN"
        const val DEFAULT_PASSWORD = "NOTFASCIST"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    var isDarkMode: Boolean
        get() = prefs?.getBoolean(KEY_DARK_MODE, false) ?: false // LIGHT MODE by default!
        set(value) { prefs?.edit()?.putBoolean(KEY_DARK_MODE, value)?.apply() }

    var baseUrl: String
        get() = prefs?.getString(KEY_BASE_URL, DEFAULT_BASE_URL)?.trimEnd('/') ?: DEFAULT_BASE_URL
        set(value) { prefs?.edit()?.putString(KEY_BASE_URL, value.trimEnd('/'))?.apply() }

    var username: String
        get() = prefs?.getString(KEY_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME
        set(value) { prefs?.edit()?.putString(KEY_USERNAME, value)?.apply() }

    var password: String
        get() = prefs?.getString(KEY_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
        set(value) { prefs?.edit()?.putString(KEY_PASSWORD, value)?.apply() }

    var isLoggedIn: Boolean
        get() = prefs?.getBoolean(KEY_IS_LOGGED_IN, true) ?: true // default true with verified credentials
        set(value) { prefs?.edit()?.putBoolean(KEY_IS_LOGGED_IN, value)?.apply() }

    fun getBasicAuthHeader(): String {
        val creds = "$username:$password"
        val encoded = try {
            Base64.encodeToString(creds.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        } catch (e: Throwable) {
            java.util.Base64.getEncoder().encodeToString(creds.toByteArray(Charsets.UTF_8))
        }
        return "Basic $encoded"
    }

    fun saveCredentials(url: String, user: String, pass: String) {
        prefs?.edit()
            ?.putString(KEY_BASE_URL, url.trimEnd('/'))
            ?.putString(KEY_USERNAME, user)
            ?.putString(KEY_PASSWORD, pass)
            ?.putBoolean(KEY_IS_LOGGED_IN, true)
            ?.apply()
    }

    fun logout() {
        prefs?.edit()?.putBoolean(KEY_IS_LOGGED_IN, false)?.apply()
    }
}
