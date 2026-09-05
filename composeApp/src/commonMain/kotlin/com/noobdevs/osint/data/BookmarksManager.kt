package com.noobdevs.osint.data

import com.noobdevs.osint.data.models.PostItem
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BookmarksManager(private val settings: Settings = Settings()) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val KEY_SAVED_POSTS = "saved_posts_json"

    private val _bookmarksFlow = MutableStateFlow<List<PostItem>>(loadSavedList())
    val bookmarksFlow: StateFlow<List<PostItem>> = _bookmarksFlow.asStateFlow()

    private fun loadSavedList(): List<PostItem> {
        val serialized = settings.getString(KEY_SAVED_POSTS, "")
        if (serialized.isBlank()) return emptyList()
        return try {
            json.decodeFromString<List<PostItem>>(serialized)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistList(list: List<PostItem>) {
        val serialized = json.encodeToString(list)
        settings.putString(KEY_SAVED_POSTS, serialized)
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
