package com.noobdevs.osint.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookmarksManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("osint_evidence_bag", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val KEY_SAVED_POSTS = "saved_posts_json"

    private val _bookmarksFlow = MutableStateFlow<List<PostItem>>(loadSavedList())
    val bookmarksFlow: StateFlow<List<PostItem>> = _bookmarksFlow.asStateFlow()

    private fun loadSavedList(): List<PostItem> {
        val json = prefs.getString(KEY_SAVED_POSTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<PostItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistList(list: List<PostItem>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_SAVED_POSTS, json).apply()
        _bookmarksFlow.value = list
    }

    fun isBookmarked(postId: Long): Boolean {
        return _bookmarksFlow.value.any { it.id == postId }
    }

    fun toggleBookmark(post: PostItem): Boolean {
        val current = _bookmarksFlow.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == post.id }
        val isNowBookmarked: Boolean
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
            isNowBookmarked = false
        } else {
            current.add(0, post)
            isNowBookmarked = true
        }
        persistList(current)
        return isNowBookmarked
    }
}
